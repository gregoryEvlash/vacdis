package com.vacantiedisc.inventory

import akka.util.Timeout
import com.vacantiedisc.inventory.models.{COMEDY, Performance, PerformanceCondition}
import org.joda.time.LocalDate

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Random

trait TestDataUtil {

  val TEST_GENRE = COMEDY

  implicit val timeout: Timeout = 10.seconds

  def generateNow: LocalDate = LocalDate.now()

  def generatePerformance: Performance =
    Performance(Random.nextString(20), generateNow, TEST_GENRE)

  def nextInt(i: Int): Int = Random.nextInt(10)

  def generateCondition(i: Int, discount: Int = 0): PerformanceCondition = {
    PerformanceCondition(
      startAfterDays = nextInt(i),
      endAfterDays = nextInt(i) + 10,
      capacity = nextInt(i) + i,
      discountPercent = 0,
      dailyAvailability = nextInt(i) + 1
    )
  }

  def await[T](f: Future[T]): T = Await.result(f, 55.seconds)

}
