package com.vacantiedisc.inventory.db

import org.joda.time.LocalDate

case class TimeTableRow(title: String, date: LocalDate, capacity: Int, discountPercent: Double, dailyAvailability: Int, sold: Int)
