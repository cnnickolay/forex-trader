name := "oanda-trader"

version := "0.02"

scalaVersion := "2.11.11"

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("org.nikosoft.oanda.bot.streaming.PriceStreamer")