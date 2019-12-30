package com.vacantiedisc.inventory.service

import com.vacantiedisc.inventory.config.{ConditionsConf, PriceConf}
import com.vacantiedisc.inventory.db.TimeTableRow
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.util.DateUtils
import org.joda.time.LocalDate

trait InventoryUtils {

  protected def deriveShowStatus(dbValue: TimeTableRow,
                                 queryDate: LocalDate,
                                 sellingStartBeforeDays: Int): ShowStatus = {
    import dbValue._
    date match {
      case d if d.isBefore(queryDate) => InThePast
      case d if d.isAfter(queryDate) && DateUtils.getDaysGap(d, queryDate) > sellingStartBeforeDays => SaleNotStarted
      case _ if sold >= capacity => SoldOut
      case _                     => OpenForSale
    }
  }

  protected def derivePrice(genre: Genre, priceConf: PriceConf): Double =
    genre match {
      case MUSICAL => priceConf.musical
      case COMEDY  => priceConf.comedy
      case DRAMA   => priceConf.drama
    }

  protected def calculatePrice(price: Double, discountPercent: Double): Double =
    price * (100 - discountPercent) / 100

  protected def buildShowInfo(
    row: TimeTableRow,
    genre: Genre,
    availability: Map[String, Int],
    queryDate: LocalDate,
    sellingStartBeforeDays: Int
  )(priceConf: PriceConf): ShowInfo = {
    import row._
    ShowInfo(
      title = title,
      ticketsLeft = capacity - sold,
      ticketsAvailable = dailyAvailability - availability.getOrElse(title, 0),
      status = deriveShowStatus(row, queryDate, sellingStartBeforeDays),
      price = calculatePrice(derivePrice(genre, priceConf), discountPercent)
    )
  }
}
