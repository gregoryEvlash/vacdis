package com.vacantiedisc.inventory

import com.vacantiedisc.inventory.models.{COMEDY, Performance, PerformanceCondition}
import org.joda.time.LocalDate

import scala.util.Random

trait TestDataUtil {

  def generateNow: LocalDate = LocalDate.now()

  def generatePerformance =
    Performance(Random.nextString(20), generateNow, COMEDY)

  def nextInt(i: Int) = Random.nextInt(10)

  def generateCondition(i: Int, discount: Int = 0) = {
    PerformanceCondition(
      startAfterDays = nextInt(i),
      endAfterDays = nextInt(i) + 10,
      capacity = nextInt(i),
      discountPercent = 0,
      dailyAvailability = nextInt(i)
    )
  }
}
