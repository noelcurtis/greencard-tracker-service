package com.noelcurtis.greencard

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import play.api.libs.json._

package object bullitenscraper {

  case class Bulletin(date: String,
                      finalActionFamilySponsoredTable: List[FamilySponsoredRow],
                      familySponsoredTable: List[FamilySponsoredRow],
                      finalActionEmploymentSponsoredTable: List[EmploymentSponsoredRow],
                      employmentSponsoredTable: List[EmploymentSponsoredRow])

  case class FamilySponsoredRow(family: String, chargeabilityAreas: DateOrCurrent, china: DateOrCurrent, india: DateOrCurrent,
                                mexico: DateOrCurrent, phillippines: DateOrCurrent)

  case class EmploymentSponsoredRow(family: String, chargeabilityAreas: DateOrCurrent, china: DateOrCurrent,
                                    india: DateOrCurrent, mexico: DateOrCurrent, phillippines: DateOrCurrent)

  case class DateOrCurrent(date: Option[String] = None, isCurrent: Boolean = true)

  implicit val DateOrCurrentWrites: Writes[DateOrCurrent] = Json.writes[DateOrCurrent]

  implicit val FamilySponsoredRowWrites: Writes[FamilySponsoredRow] = Json.writes[FamilySponsoredRow]

  implicit val EmploymentSponsoredRowWrites: Writes[EmploymentSponsoredRow] = Json.writes[EmploymentSponsoredRow]

  implicit val BullitenWrites: Writes[Bulletin] = Json.writes[Bulletin]

  object DateOrCurrent {

    val Format: DateTimeFormatter = DateTimeFormatter.ofPattern("dMMMyy", Locale.ENGLISH)

    def fromString(value: String): DateOrCurrent = {
      if (value.toLowerCase().equals("c")) {
        DateOrCurrent(isCurrent = true)
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
