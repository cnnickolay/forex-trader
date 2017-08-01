name := "trading-bot"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.12" % "2.5.3",
  "com.typesafe.akka" % "akka-stream_2.12" % "2.5.3",
  "com.typesafe.akka" % "akka-stream-contrib_2.12" % "0.8"
)

