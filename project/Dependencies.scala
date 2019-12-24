package com.vacantiedisc.inventory

import sbt._

object Dependencies {

  val jodaTime = "joda-time" % "joda-time" % "2.10.5"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"

  val cats = "org.typelevel" %% "cats-core" % "2.0.0"

  val log = Seq("ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  )

  val json = Seq(
    "io.circe" %% "circe-core" % "0.12.3",
    "io.circe" %% "circe-generic" % "0.12.3",
    "io.circe" %% "circe-parser" % "0.12.3",
    "de.heikoseeberger" %% "akka-http-circe" % "1.30.0"
  )

  val csv = "com.github.tototoshi" %% "scala-csv" % "1.3.6"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.6.1",
    "com.typesafe.akka" %% "akka-http" % "10.1.11"
  )

  val all = Seq(jodaTime, scalatest, cats, csv) ++ log ++ akka ++ json
}
