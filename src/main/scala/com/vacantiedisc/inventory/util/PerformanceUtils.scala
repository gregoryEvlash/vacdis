package com.vacantiedisc.inventory.util

import com.vacantiedisc.inventory.models._

object PerformanceUtils {

  def applyRule(performance: Performance)(rule: Rule): Seq[TimeTable] = {
    import performance._
    (rule.startAfterDays until rule.endAfterDays).map{ i =>
      TimeTable(title, date.plusDays(i), rule.capacity, rule.discountPercent, rule.dailyAvailability)
    }
  }

  def convertToTimeTable(performance: Performance): Seq[TimeTable] = {
    val forRule = applyRule(performance) _
    // todo possible from config
    forRule(BigHall) ++ forRule(SmallHall) ++ forRule(SmallHallWithDiscount)

  }

}
