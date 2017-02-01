package com.noelcurtis.greencard

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.{Date, Locale}

import play.api.libs.json._

package object bullitenscraper {

  case class Bulliten(date: String, finalActionFamilySponsoredTable: FamilySponsoredTable,
                      familySponsoredTable: FamilySponsoredTable, finalActionEmploymentSponsoredTable: EmploymentSponsoredTable,
                      employmentSponsoredTable: EmploymentSponsoredTable)

  case class FamilySponsoredTable(rowList: List[FamilySponsoredRow])

  case class FamilySponsoredRow(family: String, chargeabilityAreas: DateOrCurrent, china: DateOrCurrent, india: DateOrCurrent,
                                mexico: DateOrCurrent, phillippines: DateOrCurrent)

  case class EmploymentSponsoredTable(rowList: List[EmploymentSponsoredRow])

  case class EmploymentSponsoredRow(family: String, chargeabilityAreas: DateOrCurrent, china: DateOrCurrent,
                                    india: DateOrCurrent, mexico: DateOrCurrent, phillippines: DateOrCurrent)

  case class DateOrCurrent(date: Option[String] = None, current: Boolean = true)

  implicit val DateOrCurrentWrites: Writes[DateOrCurrent] = Json.writes[DateOrCurrent]

  implicit val FamilySponsoredTableWrites: Writes[FamilySponsoredTable] = Json.writes[FamilySponsoredTable]

  implicit val FamilySponsoredRowWrites: Writes[FamilySponsoredRow] = Json.writes[FamilySponsoredRow]

  implicit val EmploymentSponsoredTableWrites: Writes[EmploymentSponsoredTable] = Json.writes[EmploymentSponsoredTable]

  implicit val EmploymentSponsoredRowWrites: Writes[EmploymentSponsoredRow] = Json.writes[EmploymentSponsoredRow]

  implicit val BullitenWrites: Writes[Bulliten] = Json.writes[Bulliten]

  object DateOrCurrent {

    val Format: DateTimeFormatter = DateTimeFormatter.ofPattern("dMMMyy", Locale.ENGLISH)

    def fromString(value: String): DateOrCurrent = {
      if (value.toLowerCase().equals("c")) {
        DateOrCurrent(current = true)
      } else {
        // 	22MAR15 -> 22Mar15
        val day = value.toCharArray.subSequence(0, 2).toString
        val year = value.toCharArray.subSequence(5, 7).toString
        val month = value.toCharArray.subSequence(2, 5).toString.toLowerCase.split(' ').map(_.capitalize).mkString(" ").trim
        // parse date
        val localDate = LocalDate.parse(day + month + year, Format)
        DateOrCurrent(Some(localDate.toString), false)
      }
    }

  }


}
