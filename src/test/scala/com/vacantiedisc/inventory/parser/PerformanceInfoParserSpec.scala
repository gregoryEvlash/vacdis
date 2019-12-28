package com.vacantiedisc.inventory.parser

import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.util.DateUtils
import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

import scala.util.Random

class PerformanceInfoParserSpec extends WordSpec with Matchers {

  val listCSVCorrect = List(Random.nextString(10), LocalDate.now().toString(DateUtils.timeFormat), "DRAMA")
  val listCSVWrong = List(Random.nextString(10), Random.nextString(10))

  "PerformanceInfoParser" should {

    "parse genre" in {
      PerformanceInfoParser.parseGenre("COMEDY") shouldBe Some(COMEDY)
      PerformanceInfoParser.parseGenre("DRAMA") shouldBe Some(DRAMA)
      PerformanceInfoParser.parseGenre("MUSICAL") shouldBe Some(MUSICAL)
      PerformanceInfoParser.parseGenre(Random.nextString(5)) shouldBe None
    }

    "parse genre case insensitive" in {

      val gauge = Some(COMEDY)
      val comedyCapital = "Comedy"
      val comedyLower = "comedy"
      val comedyRand = "cOmEdy"

      PerformanceInfoParser.parseGenre(comedyCapital) shouldBe gauge
      PerformanceInfoParser.parseGenre(comedyLower) shouldBe gauge
      PerformanceInfoParser.parseGenre(comedyRand) shouldBe gauge
    }

    "parse csv row" in {
      PerformanceInfoParser.parse(listCSVCorrect).isDefined shouldBe true
    }

    "fail with wrong format" in {
      PerformanceInfoParser.parse(listCSVWrong) shouldBe None

    }
  }

}
