package com.vacantiedisc.inventory.http.models

import org.joda.time.LocalDate

case class BookingRequest(title: String, date: LocalDate, amount: Int)
