package com.noelcurtis.greencard.bullitenscraper

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.json.Json

import scala.collection.JavaConversions._

class BullitenManager(baseUrl: String = "https://travel.state.gov/content/visas/en/law-and-policy/bulletin.html")(implicit jsoupManager: JsoupManager) extends LazyLogging {

  val recentDateFormat = DateTimeFormatter.ofPattern("yyyy-MMMM-dd")

  def fetchLatestBulliten(): Bulliten = {
    val bullitenList: Document = jsoupManager.getDocument(baseUrl)
    val recentBullitenUrl: String = "https://travel.state.gov" + bullitenList.select("div.recentbulletins a").first().attr("href")
    val recentDate = parseRecentDate(recentBullitenUrl)
    logger.info("Found latest bulliten url [{}] and date [{}]", recentBullitenUrl, recentDate)

    val latestBulliten: Document = jsoupManager.getDocument(recentBullitenUrl)
    val elements: Elements = latestBulliten.select("table.grid")
    logger.debug(s"Found [${elements.size()}] tables")

    logger.info("Parsing tables ...")
    val finalActionFamilySponsoredTable: FamilySponsoredTable = FamilySponsoredTable(parseFamilySponsoredRows(elements, 0))
    val familySponsoredTable: FamilySponsoredTable = FamilySponsoredTable(parseFamilySponsoredRows(elements, 1))
    val finalActionEmploymentSponsoredTable: EmploymentSponsoredTable = EmploymentSponsoredTable(parseEmploymentSponsoredRows(elements, 2))
    val employmentSponsoredTable: EmploymentSponsoredTable = EmploymentSponsoredTable(parseEmploymentSponsoredRows(elements, 3))

    val bulliten: Bulliten = Bulliten(
      recentDate,
      finalActionFamilySponsoredTable,
      familySponsoredTable,
      finalActionEmploymentSponsoredTable,
      employmentSponsoredTable
    )
    logger.info("Completed building Bulliten for url [{}] and date [{}]", recentBullitenUrl, recentDate)
    logger.info("Bulliten {}", bulliten)
//    val jsValue = Json.toJson(bulliten)
    println(new ObjectMapper().writeValueAsString(bulliten))
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
