package com.vacantiedisc.inventory.utils

import com.vacantiedisc.inventory.models.Rule
import org.joda.time.{DateTime, Days, LocalDate}
import org.joda.time.format.DateTimeFormat

import scala.util.Try

object DateUtils {

  val timeFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  def toDateTime(s: String): Option[DateTime] = {
    Try(DateTime.parse(s, timeFormat)).toOption
  }

  def getDaysGap(targetDate: LocalDate, baseDate: LocalDate): Int =
    Days.daysBetween(targetDate, baseDate).getDays
}
