package com.vacantiedisc.inventory.json

import com.vacantiedisc.inventory.models.{SaleNotStarted, ShowInfo}
import com.vacantiedisc.inventory.json.response.response._
import org.scalatest.{Matchers, WordSpec}

class JsonParserSpec extends WordSpec with Matchers {

  "JsonParser" should {

    "parse win snake case " in {
      val gauge =
        """|{
           |  "title" : "LA MUSICA",
           |  "tickets_left" : 200,
           |  "tickets_available" : 10,
           |  "status" : "sale not started",
           |  "price" : 40.0
           |}""".stripMargin
      val showInfo = ShowInfo("LA MUSICA", 200, 10, SaleNotStarted, 40)
      val result = showInfoEncoder(showInfo).toString()
      result shouldBe gauge
    }

  }
}
