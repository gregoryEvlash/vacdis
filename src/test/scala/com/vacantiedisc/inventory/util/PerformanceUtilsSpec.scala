package com.vacantiedisc.inventory.util

import com.vacantiedisc.inventory.config.ConditionsConf
import com.vacantiedisc.inventory.models.{
  COMEDY,
  Performance,
  PerformanceCondition
}
import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

import scala.util.Random

class PerformanceUtilsSpec extends WordSpec with Matchers {

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

  private def generateNow: LocalDate = LocalDate.now()

  private def generatePerformance =
    Performance(Random.nextString(20), generateNow, COMEDY)

  private def nextInt(i: Int) = Random.nextInt(10)

  private def generateCondition(i: Int, discount: Int = 0) = {
    PerformanceCondition(
      startAfterDays = nextInt(i),
      endAfterDays = nextInt(i) + 10,
      capacity = nextInt(i),
      discountPercent = 0,
      dailyAvailability = nextInt(i)
    )
  }
}
