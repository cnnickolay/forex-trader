name := "oanda-trader"

version := "0.02"

scalaVersion := "2.12.4"

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("org.nikosoft.oanda.bot.streaming.PriceStreamer")