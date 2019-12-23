package com.vacantiedisc.inventory.service

import java.io.File

import com.github.tototoshi.csv.CSVReader
import com.vacantiedisc.inventory.models.Performance
import com.vacantiedisc.inventory.parser.PerformanceInfoParser


import scala.util.Try

object FileService {
  def parseFile(path: String): Seq[Performance] = {
    Try {
      val reader = CSVReader.open(new File(path))
      val data =  reader.all().flatMap(PerformanceInfoParser.parse)
      reader.close()
      data
    }.recover{
      case t =>
      println(s"Unable to parse file ${t.getMessage}") // todo to log
      Seq.empty[Performance]
    }.get
  }
}