package com.vacantiedisc.inventory.util

import org.joda.time.{DateTime, Days, LocalDate}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.util.Try

object DateUtils {

  val timeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  def toDateTime(s: String): Option[LocalDate] = {
    Try(DateTime.parse(s, timeFormat)).toOption.map(_.toLocalDate)
  }

  def getDaysGap(targetDate: LocalDate, baseDate: LocalDate): Int =
    Math.abs(Days.daysBetween(targetDate, baseDate).getDays)

}
