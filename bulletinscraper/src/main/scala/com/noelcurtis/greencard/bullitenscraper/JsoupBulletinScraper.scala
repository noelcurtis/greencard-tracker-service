package com.noelcurtis.greencard.bullitenscraper

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.Config
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.json.Json

import scala.collection.JavaConversions._

trait BulletinScraper {
  def fetchLatestBulliten(): Bulletin
}

class JsoupBulletinScraper(config: Config)(implicit jsoupManager: JsoupManager)
  extends BulletinScraper with LazyLogging {

  lazy val recentDateFormat = DateTimeFormatter.ofPattern("yyyy-MMMM-dd")
  val baseUrl: String = config.getString("app.visaBulletinBaseUrl") + config.getString("app.rootBulletinPath")

  def fetchLatestBulliten(): Bulletin = {
    logger.info("Starting to fetch latest Bulletin")
    val bulletinList: Document = jsoupManager.getDocument(baseUrl)
    val recentBulletinUrl: String = config.getString("app.visaBulletinBaseUrl") + bulletinList.select("div.recentbulletins a").first().attr("href")
    val recentDate = parseRecentDate(recentBulletinUrl)
    logger.info("Found latest bulletin url [{}] and date [{}]", recentBulletinUrl, recentDate)

    val latestBulliten: Document = jsoupManager.getDocument(recentBulletinUrl)
    val elements: Elements = latestBulliten.select("table.grid")
    logger.info(s"Found [${elements.size()}] tables")
    logger.info("Parsing tables ...")

    val bulliten: Bulletin = Bulletin(
      recentDate,
      parseFamilySponsoredRows(elements, 0),
      parseFamilySponsoredRows(elements, 3),
      parseEmploymentSponsoredRows(elements, 4),
      parseEmploymentSponsoredRows(elements, 5)
    )

    logger.info("Completed building Bulliten for url [{}] and date [{}]", recentBulletinUrl, recentDate)
    logger.debug("Bulliten {}", Json.toJson(bulliten))
    bulliten
  }

  def parseFamilySponsoredRows(elements: Elements, position: Int): List[FamilySponsoredRow] = {
    val elementList = elements.get(position).select("tr").toList
    logger.info(s"Parsing family sponsored rows at position [$position] with count [${elements.size()}]")

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
    logger.info(s"Parsing employment sponsored rows at position [$position] with count [${elements.size()}]")

    elementList.subList(1, elementList.size).map(element => {
      if (position == 5) {
        EmploymentSponsoredRow(family = element.select("td").get(0).html().replace("<br>", "").replace("<br>", ""),
          chargeabilityAreas = DateOrCurrent.fromString(element.select("td").get(1).html()),
          china = DateOrCurrent.fromString(element.select("td").get(2).html()),
          india = DateOrCurrent.fromString(element.select("td").get(3).html()),
          mexico = DateOrCurrent.fromString(element.select("td").get(4).html()),
          phillippines = DateOrCurrent.fromString(element.select("td").get(5).html()))
      } else {
        EmploymentSponsoredRow(family = element.select("td").get(0).html().replace("<br>", "").replace("<br>", ""),
          chargeabilityAreas = DateOrCurrent.fromString(element.select("td").get(1).html()),
          china = DateOrCurrent.fromString(element.select("td").get(2).html()),
          india = DateOrCurrent.fromString(element.select("td").get(4).html()),
          mexico = DateOrCurrent.fromString(element.select("td").get(5).html()),
          phillippines = DateOrCurrent.fromString(element.select("td").get(6).html()),
          elSalvadorGuatamelaHonduras = Some(DateOrCurrent.fromString(element.select("td").get(3).html()))
        )

      }
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
