package com.vacantiedisc.inventory.validation

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import com.vacantiedisc.inventory.http.models.{BookingOverviewRequest, BookingRequest}
import com.vacantiedisc.inventory.util.DateUtils
import com.vacantiedisc.inventory.validation.Validation._
import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

class ValidationSpec extends WordSpec with Matchers {

  "Validation BookingRequest" should {

    "pass properly" in {
      val request = BookingRequest("Test", LocalDate.now, 1)
      Validation.bookingRequestValidator.validate(request) shouldBe Valid(request)
    }

    "error on short list" in {
      val request = BookingRequest("Test", LocalDate.now, -88)
      Validation.bookingRequestValidator.validate(request) shouldBe Invalid(NonEmptyList(WRONG_AMOUNT_FORMAT, Nil))
    }

  }

  "Validation BookingOverviewRequest" should {

    "pass properly" in {
      val date = LocalDate.now()
      val request = BookingOverviewRequest(date.toString(DateUtils.timeFormat))
      Validation.dateValidator.validate(request) shouldBe Valid(date)
    }

    "error on short list" in {
      val request = BookingOverviewRequest("test_123")
      Validation.dateValidator.validate(request) shouldBe Invalid(NonEmptyList(WRONG_DATE_FORMAT, Nil))
    }

  }

}
