name := "oanda-scala-api"

version := "1.0"

scalaVersion := "2.11.11"

val json4sVersion = "3.2.11"
val akkaVersion = "2.5.4"
val httpComponentsVersion = "4.5.3"
libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion,
  "org.apache.httpcomponents" % "fluent-hc" % httpComponentsVersion,
  "org.json4s" % "json4s-jackson_2.11" % json4sVersion,
  "org.json4s" % "json4s-ext_2.11" % json4sVersion,
  "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
  "com.typesafe.akka" % "akka-stream_2.11" % akkaVersion,
  "joda-time" % "joda-time" % "2.9.9",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.27",
  "com.typesafe.akka" % "akka-http-core_2.11" % "10.0.9"
)
