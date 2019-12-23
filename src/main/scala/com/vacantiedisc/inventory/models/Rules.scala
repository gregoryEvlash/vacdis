package com.vacantiedisc.inventory.models

sealed trait Rule

case object BigHall extends Rule
case object SmallHall extends Rule
case object SmallHallWithDiscount extends Rule

// todo to config
object Rule {

    val startSellingDaysBefore  = 25

    val performanceDurationDays = 100
    val discountAfterDays       = 80
    val smallHallAfterDays      = 60

    val capacityBig             = 200
    val capacitySmall           = 100

    val maxAvailabilityBig      = 10
    val maxAvailabilitySmall    = 5
}
