package com.vacantiedisc.inventory.util

import com.vacantiedisc.inventory.models._
import Rule._
import com.vacantiedisc.inventory.utils.DateUtils
import org.joda.time.{Days, LocalDate}

object PerformanceUtils {

  def getSellingDates(firstDate: LocalDate): Seq[LocalDate] = {
    (0 until performanceDurationDays).map{firstDate.plusDays}
  }

  def deriveRule(targetDate: LocalDate, baseDate: LocalDate): Rule =
    DateUtils.getDaysGap(targetDate, baseDate) match {
      case x if x > discountAfterDays  && x <= performanceDurationDays => SmallHallWithDiscount
      case x if x > smallHallAfterDays && x <= discountAfterDays       => SmallHall
      case _                                                           => BigHall
    }

  def ruleToCapacity(rule: Rule) = {
    rule match {
      case SmallHall => 100
      case SmallHallWithDiscount => 100
      case _ => 200
    }
  }

  def ruleToAvailability(rule: Rule) = {
    rule match {
      case SmallHall => 5
      case SmallHallWithDiscount => 5
      case _ => 10
    }
  }


//  def deriveMaxCapacity(targetDate: LocalDate, baseDate: LocalDate = LocalDate.now()) = {
//    baseDate match {
//
//      case x
//
//    }
//  }

}
