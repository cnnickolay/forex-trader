import sbt.Keys._

name := "trading-bot"

version := "1.0"

scalaVersion := "2.12.4"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.8.7"

val akkaDependencies = {
  val version = "2.5.4"
  Seq(
    "com.typesafe.akka" % "akka-actor_2.12" % version,
    "com.typesafe.akka" % "akka-stream_2.12" % version,
    "com.typesafe.akka" % "akka-stream-contrib_2.12" % "0.8",
    "com.typesafe.akka" % "akka-stream-kafka_2.12" % "0.17"
  )
}

val otherDependendies = Seq("org.apache.commons" % "commons-math3" % "3.6.1")

libraryDependencies ++= akkaDependencies ++ otherDependendies