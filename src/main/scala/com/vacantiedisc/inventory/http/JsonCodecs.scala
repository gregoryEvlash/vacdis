package com.vacantiedisc.inventory.http

import com.vacantiedisc.inventory.http.models.BookingRequest
import com.vacantiedisc.inventory.models._
import io.circe._
import io.circe.generic.auto._
import org.joda.time.LocalDate

object JsonCodecs {
  implicit val bookingRequestDecoder: Decoder[BookingRequest] =
    Decoder[BookingRequest]

  implicit val inventoryServiceResponseFormat
    : Encoder[InventoryServiceResponse] = Encoder[InventoryServiceResponse]

  implicit val bookedResponseFormat: Encoder[BookedResponse] =
    Encoder[BookedResponse]
  implicit val overviewResponseFormat: Encoder[OverviewResponse] =
    Encoder[OverviewResponse]
  implicit val dateEncoder: Encoder[LocalDate] = Encoder[LocalDate]

  implicit val inventoryResponseEncoder: Encoder[InventoryResponse] =
    Encoder[InventoryResponse]

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

  implicit val inventoryErrorEncoder: Encoder[InventoryError] =
    Encoder[InventoryError]
}
