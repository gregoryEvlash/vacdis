package com.vacantiedisc.inventory.validation

import com.vacantiedisc.inventory.http.models.{
  BookingOverviewRequest,
  BookingRequest
}
import com.vacantiedisc.inventory.util.DateUtils
import org.joda.time.LocalDate

/**
  * Validate row entity
  * for now it validates only credit limit and birthday
  * possible to extend check postcode, phone number etc
  */
object Validation {

  import cats.data.ValidatedNel
  import cats.implicits._

  trait Validator[T, U] {
    def validate(entity: T): ValidatedNel[String, U]
  }

  object Validator {
    def validate[T, U](entity: T)(implicit v: Validator[T, U]): ValidatedNel[String, U] =
      v.validate(entity)
  }

  val WRONG_DATE_FORMAT = "Wrong Date time format"
  val WRONG_AMOUNT_FORMAT = "Amount must be more than 0"

  def validateAmount(br: BookingRequest): ValidatedNel[String, BookingRequest] = {
    Either.cond(br.amount > 0, br, WRONG_AMOUNT_FORMAT).toValidatedNel
  }

  implicit val bookingRequestValidator
    : Validator[BookingRequest, BookingRequest] = { entity: BookingRequest =>
    validateAmount _ apply entity
  }

  def validateDateFormat(request: BookingOverviewRequest): ValidatedNel[String, LocalDate] = {
    val parsedDate = DateUtils.toDateTime(request.performanceDate)
    Either
      .cond(parsedDate.isDefined, parsedDate.get, WRONG_DATE_FORMAT)
      .toValidatedNel
  }

  implicit val dateValidator: Validator[BookingOverviewRequest, LocalDate] = {
    entity =>
      validateDateFormat _ apply entity
  }

}
