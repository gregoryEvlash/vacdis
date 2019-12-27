package com.vacantiedisc.inventory.util

import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

import scala.util.Random

class DateUtilsSpec  extends WordSpec with Matchers  {

  "DateUtils" should {

    "parse date properly" in {
      val now = LocalDate.now
      DateUtils.toDateTime(now.toString(DateUtils.timeFormat)) shouldBe Some(now)
    }

    "ignore wrong format" in {
      val oddDate = "X.VI.67 b.c."

      DateUtils.toDateTime(oddDate) shouldBe None
    }

    "days gap should be positive" in {
      val now = LocalDate.now
      val anotherDate = LocalDate.now.plusDays(Random.nextInt(100))

      DateUtils.getDaysGap(now, anotherDate) shouldBe DateUtils.getDaysGap(anotherDate, now)
    }

  }

}
