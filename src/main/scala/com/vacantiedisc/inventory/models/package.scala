package com.vacantiedisc.inventory

import io.circe.generic.extras.ConfiguredJsonCodec
import org.joda.time.LocalDate

package object models {

  sealed trait Genre
  case object MUSICAL extends Genre
  case object COMEDY  extends Genre
  case object DRAMA   extends Genre

  case class Performance(title: String, date: LocalDate, genre: Genre)
  case class Show(title: String, date: LocalDate)
  case class TimeTable(title: String, date: LocalDate, capacity: Int, discountPercent: Double, dailyAvailability: Int)

  sealed trait ShowStatus
  case object SaleNotStarted extends ShowStatus
  case object OpenForSale    extends ShowStatus
  case object SoldOut        extends ShowStatus
  case object InThePast      extends ShowStatus

  case class DBSearchResult(show: Show, bought: Int)

  case class ShowInfo(title: String,
                      ticketsLeft: Int,
                      ticketsAvailable: Int,
                      status: ShowStatus,
                      price: Double)
  case class InventoryResult(genre: Genre, shows: Seq[ShowInfo])




  sealed trait InventoryError
  case class TicketsSoldOut(title: String, date: LocalDate) extends InventoryError
  case class NotStarted(title: String)                      extends InventoryError
  case class NotFound(title: String)                        extends InventoryError
  case class Custom(message: String)                        extends InventoryError
  case class Validation(errors: Seq[String])                extends InventoryError

  sealed trait InventoryResponse
  case class BookedResponse(show: Show, amount: Int)           extends InventoryResponse
  case class OverviewResponse(inventory: Seq[InventoryResult]) extends InventoryResponse

  type InventoryServiceResponse = Either[InventoryError, InventoryResponse]

}
