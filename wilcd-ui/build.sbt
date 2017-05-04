import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import com.jamesward.play.BrowserNotifierKeys

name := """wilcd-ui"""
organization := "se.kth.wilcd"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, JDebPackaging, SystemdPlugin)

scalaVersion := "2.11.8"

resolvers += "webjars" at "https://dl.bintray.com/webjars/maven"

BrowserNotifierKeys.shouldOpenBrowser := false

val webpackWebTask = taskKey[Seq[File]]("Sbt-Webpack adapter for sbt-web")

webpackWebTask := Def.task {
  (WebKeys.webTarget.value / "webpack").listFiles().toSeq
}.dependsOn(webpack.toTask("")).value

sourceGenerators in Assets += webpackWebTask.taskValue

pipelineStages := Seq(digest, gzip)

libraryDependencies ++= Seq(
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.seleniumhq.selenium" % "selenium-java" % "3.3.1" % Test,
  "org.seleniumhq.selenium" % "selenium-support" % "3.3.1" % Test,
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "3.3.1" % Test,
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.3.1" % Test,
  "com.google.guava" % "guava" % "21.0",
  "com.typesafe.play" %% "play-slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.1.0",
  "org.postgresql" % "postgresql" % "42.0.0",
  //  "org.postgresql" % "postgreqsl" % "9.4.1212",
  "com.github.tminglei" %% "slick-pg" % "0.15.0-RC",
  "org.abstractj.kalium" % "kalium" % "0.5.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.6"
)

// https://youtrack.jetbrains.com/issue/SCL-11141
managedResourceDirectories in Test += baseDirectory.value / "target/web/public/test"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "se.kth.wilcd.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "se.kth.wilcd.binders._"

routesImport ++= Seq("models._", "utils.ExtraBinders._")
