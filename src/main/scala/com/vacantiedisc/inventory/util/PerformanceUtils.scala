package com.vacantiedisc.inventory.util

import com.vacantiedisc.inventory.config.ConditionsConf
import com.vacantiedisc.inventory.models._
import org.joda.time.LocalDate

object PerformanceUtils {

  def applyRule(performance: Performance)(rule: PerformanceCondition): Seq[TimeTable] = {
    import performance._
    (rule.startAfterDays until rule.endAfterDays).map{ i =>
      TimeTable(title, date.plusDays(i), rule.capacity, rule.discountPercent, rule.dailyAvailability)
    }
  }

  def convertToTimeTable(performance: Performance)(conf: ConditionsConf): Seq[TimeTable] = {
    val forRule = applyRule(performance) _
    forRule(conf.big) ++ forRule(conf.smallRegular) ++ forRule(conf.smallDiscount)
  }

  val delim = "_-_"

  def buildKey(title: String, performanceDate: LocalDate): String = {
    s"${performanceDate.toString(DateUtils.timeFormat)}$delim$title"
  }

}
