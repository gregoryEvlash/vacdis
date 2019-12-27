package com.vacantiedisc.inventory

import com.vacantiedisc.inventory.http.models.{BookingOverviewRequest, BookingRequest}
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.util.DateUtils
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto._
import org.joda.time.LocalDate

package object json {
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val bookingOverviewRequestDecoder: Decoder[BookingOverviewRequest] = deriveDecoder[BookingOverviewRequest]
  implicit val bookingRequestDecoder: Decoder[BookingRequest] = deriveDecoder[BookingRequest]

  val localDateDecoderError = DecodingFailure("Wrong date format", Nil)
  implicit val dateDecoder: Decoder[LocalDate] = _.value match {
    case j if j.isString => DateUtils.toDateTime(j.asString.getOrElse("")).toRight[DecodingFailure](localDateDecoderError)
    case _ => Left(localDateDecoderError)
  }
  implicit val dateEncoder: Encoder[LocalDate] = x => Json.fromString(x.toString(DateUtils.timeFormat))

  implicit val inventoryServiceResponseFormat: Encoder[InventoryServiceResponse] = Encoder[InventoryServiceResponse]

  implicit val showStatusEncoder: Encoder[ShowStatus] = {
    case SaleNotStarted => Json.fromString("sale not started")
    case OpenForSale =>Json.fromString("open for sale")
    case SoldOut =>Json.fromString("sold out")
    case InThePast =>Json.fromString("in the past")
  }
  implicit val genreEncoder: Encoder[Genre] = {
    case MUSICAL => Json.fromString("musical")
    case COMEDY =>Json.fromString("comedy")
    case DRAMA =>Json.fromString("drama")
  }
  implicit val showInfoEncoder: Encoder[ShowInfo] = deriveEncoder[ShowInfo]
  implicit val inventoryResultEncoder: Encoder[InventoryResult] = deriveEncoder[InventoryResult]

  implicit val bookedResponseFormat: Encoder[BookedResponse] = deriveEncoder[BookedResponse]
  implicit val overviewResponseFormat: Encoder[OverviewResponse] = deriveEncoder[OverviewResponse]

  implicit val inventoryResponseEncoder: Encoder[InventoryResponse] = {
    case v: BookedResponse => bookedResponseFormat(v)
    case v: OverviewResponse => overviewResponseFormat(v)
  }

  implicit val ticketsSoldOutErrorEncoder: Encoder[TicketsSoldOut] = { x =>
    Json.fromString(s"Tickets for ${x.title} on ${x.date} is sold out")
  }
  implicit val NotStartedErrorEncoder: Encoder[NotStarted] = { x =>
    Json.fromString(s"Selling tickets on ${x.title} is not started yet")
  }
  implicit val NotFoundErrorEncoder: Encoder[NotFound] = { x =>
    Json.fromString(s"Performance ${x.title} not found")
  }
  implicit val customErrorEncoder: Encoder[Custom] = { x =>
    Json.fromString(s"There is error appears ${x.message}")
  }
  implicit val validationErrorEncoder: Encoder[Validation] = { x =>
    Json.fromString(
      s"There is some validation errors ${x.errors.mkString(",")}"
    )
  }

  implicit val inventoryErrorEncoder: Encoder[InventoryError] = {
    case v: TicketsSoldOut => ticketsSoldOutErrorEncoder(v)
    case v: NotStarted     => NotStartedErrorEncoder(v)
    case v: NotFound       => NotFoundErrorEncoder(v)
    case v: Custom         => customErrorEncoder(v)
    case v: Validation     => validationErrorEncoder(v)
  }
}
