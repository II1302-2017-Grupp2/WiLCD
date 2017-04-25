name := """wilcd-ui"""
organization := "se.kth.wilcd"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, JDebPackaging, SystemdPlugin)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  filters,
  "org.webjars.npm" % "bootstrap" % "3.3.7",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.mockito" % "mockito-core" % "2.7.20" % Test,
  "org.seleniumhq.selenium" % "selenium-java" % "3.3.1" % Test,
  "org.seleniumhq.selenium" % "selenium-support" % "3.3.1" % Test,
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "3.3.1" % Test,
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.3.1" % Test,
  "com.google.guava" % "guava" % "21.0",
  "com.typesafe.play" %% "play-slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.1.0",
  "org.postgresql" % "postgresql" % "42.0.0",
  //  "org.postgresql" % "postgreqsl" % "9.4.1212",
  "com.github.tminglei" %% "slick-pg" % "0.15.0-RC"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "se.kth.wilcd.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "se.kth.wilcd.binders._"
