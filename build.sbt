name := "oanda-trader"

version := "0.02"

scalaVersion := "2.12.2"

mainClass in assembly := Some("org.nikosoft.oanda.bot.Launcher")
assemblyOutputPath in assembly := file(s"target/trader_${version.value}.jar")
