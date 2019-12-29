package com.vacantiedisc.inventory.models

case class PerformanceCondition(startAfterDays: Int,
                                endAfterDays: Int,
                                capacity: Int,
                                discountPercent: Double,
                                dailyAvailability: Int)
