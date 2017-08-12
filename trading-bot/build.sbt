name := "trading-bot"

version := "1.0"

scalaVersion := "2.11.11"

val sparkDependencies = {
  val version = "2.2.0"
  Seq(
    "org.apache.spark" % "spark-core_2.11" % version,
    "org.apache.spark" % "spark-streaming_2.11" % version
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