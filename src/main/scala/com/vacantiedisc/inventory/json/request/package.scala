package com.vacantiedisc.inventory.json.request

import com.vacantiedisc.inventory.http.models.{BookingOverviewRequest, BookingRequest}
import com.vacantiedisc.inventory.util.DateUtils
import io.circe.magnolia.configured.Configuration
import io.circe.magnolia.configured.decoder.semiauto._
import io.circe.{Decoder, DecodingFailure}
import org.joda.time.LocalDate

package object request {

  implicit val kebabConf: Configuration = Configuration.default.withKebabCaseMemberNames

  implicit val bookingOverviewRequestDecoder: Decoder[BookingOverviewRequest] =
    deriveConfiguredMagnoliaDecoder[BookingOverviewRequest]
  implicit val bookingRequestDecoder: Decoder[BookingRequest] = deriveConfiguredMagnoliaDecoder[BookingRequest]

  val localDateDecoderError = DecodingFailure("Wrong date format", Nil)

  implicit val dateDecoder: Decoder[LocalDate] = _.value match {
    case j if j.isString =>
      DateUtils
        .toDateTime(j.asString.getOrElse(""))
        .toRight[DecodingFailure](localDateDecoderError)
    case _ =>
      Left(localDateDecoderError)
  }
}
