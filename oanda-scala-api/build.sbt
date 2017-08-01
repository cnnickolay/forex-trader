name := "oanda-scala-api"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.3",
  "org.json4s" % "json4s-native_2.12" % "3.5.2",
  "org.json4s" % "json4s-jackson_2.12" % "3.5.2",
  "org.json4s" % "json4s-ext_2.12" % "3.5.2",
  "joda-time" % "joda-time" % "2.9.9",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.27",
  "com.typesafe.akka" % "akka-actor_2.12" % "2.5.3",
  "com.typesafe.akka" % "akka-stream_2.12" % "2.5.3",
  "com.typesafe.akka" % "akka-http-core_2.12" % "10.0.9"
)
