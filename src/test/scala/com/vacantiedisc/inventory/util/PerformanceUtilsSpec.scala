package com.vacantiedisc.inventory.util

import com.vacantiedisc.inventory.TestDataUtil
import com.vacantiedisc.inventory.config.ConditionsConf
import org.scalatest.{Matchers, WordSpec}

class PerformanceUtilsSpec extends WordSpec with Matchers with TestDataUtil{

  "PerformanceUtilsSpec" should {

    "build timetable row for condition" in {
      val condition = generateCondition(10)
      val performance = generatePerformance
      val timetable = PerformanceUtils.applyRule(performance)(condition)

      timetable.size shouldBe (condition.endAfterDays - condition.startAfterDays)

      all(timetable.map(_.title)) shouldBe performance.title
      all(timetable.map(_.discountPercent)) shouldBe condition.discountPercent
      all(timetable.map(_.capacity)) shouldBe condition.capacity
      all(timetable.map(_.dailyAvailability)) shouldBe condition.dailyAvailability

      timetable
        .minBy(_.date.toString(DateUtils.timeFormat))
        .date shouldBe performance.date.plusDays(condition.startAfterDays)
      timetable
        .maxBy(_.date.toString(DateUtils.timeFormat))
        .date shouldBe performance.date.plusDays(condition.endAfterDays - 1)
    }

    "apply full config " in {
      val conf = new ConditionsConf(
        sellingStartBeforeDays = 0,
        big = generateCondition(10),
        smallRegular = generateCondition(40),
        smallDiscount = generateCondition(13, 15),
      )
      val performance = generatePerformance

      val conversionResult =
        PerformanceUtils.convertToTimeTable(performance)(conf)

      val totalDaysPerf = (conf.big.endAfterDays - conf.big.startAfterDays) +
        (conf.smallRegular.endAfterDays - conf.smallRegular.startAfterDays) +
        (conf.smallDiscount.endAfterDays - conf.smallDiscount.startAfterDays)
      conversionResult.size shouldBe totalDaysPerf

    }

  }
}
