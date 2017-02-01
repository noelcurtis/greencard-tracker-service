package com.noelcurtis.greencard.bullitenscraper

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.json.Json

import scala.collection.JavaConversions._

trait BulletinScraper {
  def fetchLatestBulliten(): Bulletin
}

class JsoupBulletinScraper(baseUrl: String = "https://travel.state.gov/content/visas/en/law-and-policy/bulletin.html")(implicit jsoupManager: JsoupManager)
  extends BulletinScraper with LazyLogging {

  lazy val recentDateFormat = DateTimeFormatter.ofPattern("yyyy-MMMM-dd")

  def fetchLatestBulliten(): Bulletin = {
    logger.info("Starting to fetch latest Bulletin")
    val bulletinList: Document = jsoupManager.getDocument(baseUrl)
    val recentBulletinUrl: String = "https://travel.state.gov" + bulletinList.select("div.recentbulletins a").first().attr("href")
    val recentDate = parseRecentDate(recentBulletinUrl)
    logger.info("Found latest bulletin url [{}] and date [{}]", recentBulletinUrl, recentDate)

    val latestBulliten: Document = jsoupManager.getDocument(recentBulletinUrl)
    val elements: Elements = latestBulliten.select("table.grid")
    logger.debug(s"Found [${elements.size()}] tables")
    logger.info("Parsing tables ...")

    val bulliten: Bulletin = Bulletin(
      recentDate,
      parseFamilySponsoredRows(elements, 0),
      parseFamilySponsoredRows(elements, 1),
      parseEmploymentSponsoredRows(elements, 2),
      parseEmploymentSponsoredRows(elements, 3)
    )

    logger.info("Completed building Bulliten for url [{}] and date [{}]", recentBulletinUrl, recentDate)
    logger.debug("Bulliten {}", Json.toJson(bulliten))
    bulliten
  }

  def parseFamilySponsoredRows(elements: Elements, position: Int): List[FamilySponsoredRow] = {
    val elementList = elements.get(position).select("tr").toList

    elementList.subList(1, elementList.size).map(element => {
      FamilySponsoredRow(family = element.select("td").get(0).html().replace("<br>", "").replace("<br>", ""),
        chargeabilityAreas = DateOrCurrent.fromString(element.select("td").get(1).html()),
        china = DateOrCurrent.fromString(element.select("td").get(2).html()),
        india = DateOrCurrent.fromString(element.select("td").get(3).html()),
        mexico = DateOrCurrent.fromString(element.select("td").get(4).html()),
        phillippines = DateOrCurrent.fromString(element.select("td").get(5).html()))
    }).toList
  }

  def parseEmploymentSponsoredRows(elements: Elements, position: Int): List[EmploymentSponsoredRow] = {
    val elementList = elements.get(position).select("tr").toList

    elementList.subList(1, elementList.size).map(element => {
      EmploymentSponsoredRow(family = element.select("td").get(0).html().replace("<br>", "").replace("<br>", ""),
        chargeabilityAreas = DateOrCurrent.fromString(element.select("td").get(1).html()),
        china = DateOrCurrent.fromString(element.select("td").get(2).html()),
        india = DateOrCurrent.fromString(element.select("td").get(3).html()),
        mexico = DateOrCurrent.fromString(element.select("td").get(4).html()),
        phillippines = DateOrCurrent.fromString(element.select("td").get(5).html()))
    }).toList
  }

  private def parseRecentDate(bullitenUrl: String): String = {
    val split = bullitenUrl.split("-")

    val year = split(split.length - 1).replace(".html", "").trim
    val month = split(split.length - 2).split(' ').map(_.capitalize).mkString(" ").trim
    val date = s"$year-$month-01"
    LocalDate.parse(date, recentDateFormat).toString
  }

}
