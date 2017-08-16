import sbt.Keys._

name := "trading-bot"

version := "1.0"

scalaVersion := "2.11.11"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"

val sparkDependencies = {
  val version = "2.2.0"
  Seq(
    "org.apache.spark" % "spark-core_2.11" % version,
    "org.apache.spark" % "spark-streaming_2.11" % version,
    "org.apache.spark" % "spark-sql_2.11" % version,
    "org.apache.spark" % "spark-mllib_2.11" % version
  )
}

val akkaDependencies = {
  val version = "2.5.4"
  Seq(
    "com.typesafe.akka" % "akka-actor_2.11" % version,
    "com.typesafe.akka" % "akka-stream_2.11" % version,
    "com.typesafe.akka" % "akka-stream-contrib_2.11" % "0.8"
  )
}

libraryDependencies ++= sparkDependencies ++ akkaDependencies