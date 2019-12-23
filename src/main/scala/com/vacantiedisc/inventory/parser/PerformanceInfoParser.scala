package com.vacantiedisc.inventory.parser

import com.vacantiedisc.inventory.models._
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.util.Try

object PerformanceInfoParser {

  val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  def parseDateTime(x: String): Option[LocalDate] = {
    Try(dateTimeFormat.parseLocalDate(x)).toOption
  }

  def parseGenre(s: String): Option[Genre] = s.toLowerCase match {
    case "musical" => Some(MUSICAL)
    case "comedy"  => Some(COMEDY)
    case "drama"   => Some(DRAMA)
    case _         => None
  }

  def parse(row: List[String]): Option[Performance] = {
    row match {
      case title :: date :: genre :: Nil =>
        for {
          dateValue  <- parseDateTime(date)
          genreValue <- parseGenre(genre)
        } yield {
          Performance(title, dateValue, genreValue)
        }
      case _ => None
    }
  }


}
