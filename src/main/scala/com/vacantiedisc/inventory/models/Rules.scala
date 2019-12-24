package com.vacantiedisc.inventory.models

sealed trait Rule {
  val startAfterDays: Int
  val endAfterDays: Int
  val capacity: Int
  val discountPercent: Double
  val dailyAvailability: Int
}

// todo to config
case object BigHall extends Rule {
    val startAfterDays: Int = 0
    val endAfterDays: Int = 60
    val capacity: Int = 200
    val discountPercent: Double = 0
    val dailyAvailability: Int = 10
}
case object SmallHall extends Rule {
    val startAfterDays: Int = 60
    val endAfterDays: Int = 80
    val capacity: Int = 100
    val discountPercent: Double = 0
    val dailyAvailability: Int = 5
}
case object SmallHallWithDiscount extends Rule{
    val startAfterDays: Int = 80
    val endAfterDays: Int = 100
    val capacity: Int = 100
    val discountPercent: Double = 20
    val dailyAvailability: Int = 5
}

// todo to config
object Rule {

    val startSellingDaysBefore  = 25

}
