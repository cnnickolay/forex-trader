package org.nikosoft.oanda.bot.streaming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration.DurationInt

object InvestingComStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val anchors = List("RSI(14)", "STOCH(9,6)", "STOCHRSI(14)", "MACD(12,26)", "ADX(14)", "Williams %R", "CCI(14)", "ATR(14)", "Highs/Lows(14)", "Ultimate Oscillator", "ROC", "Bull/Bear Power(13)")
  val averages = List("MA200", "MA100", "MA50", "MA20", "MA10", "MA5")

  Source.tick(0.minutes, 5.seconds, "tick")
    .map(_ => HttpRequest(uri = "/instruments/Service/GetTechincalData",
      method = HttpMethods.POST,
      entity = FormData(Map("pairID" -> "1", "period" -> "300", "viewType" -> "normal")).toEntity,
      headers = List(
        RawHeader("Origin", "https://www.investing.com"),
        RawHeader("Accept-Language", "en-US,en;q=0.8,ru;q=0.6,fr;q=0.4"),
        RawHeader("Accept", "*/*"),
        RawHeader("Referer", "https://www.investing.com/currencies/eur-usd-technical"),
        RawHeader("X-Requested-With", "XMLHttpRequest")
      )
    ))
    .via(Http().outgoingConnectionHttps("www.investing.com"))
    .flatMapConcat(_.entity.dataBytes.map(_.utf8String))
    .map { page =>
      val extractor = """.*>(.+?)<.*""".r
      val extractOverall = """.*(Neutral|Sell|Buy)\:.*>(\d+)<.*""".r
      val extractAverage = """\s+(\w+)\s+.*""".r
      page.split("\n").toList.sliding(15)
        .foldLeft(List.empty[(String, Any)])((result, lines) => {
          val anchor = anchors.find(lines.head.contains).map(found => {
            val extractor(advise) = lines(3)
            found -> advise
          })
          val averageSimple = averages.find(lines.head.contains).map(found => {
            val extractAverage(advise) = lines(4)
            s"${found}_simple" -> advise
          })
          val averageExponential = averages.find(lines.head.contains).map(found => {
            val extractAverage(advise) = lines(9)
            s"${found}_exponential" -> advise
          })
          val summary = if (lines.last.contains("Summary:")) Map((if (!result.exists(_._1 == "summary_technical")) "summary_technical" else "summary_averages") -> lines.collect { case extractOverall(sentiment, votes) => (sentiment, votes) }.toMap) else Map.empty[String, Any]
          result ++ anchor ++ summary ++ averageSimple ++ averageExponential
        })
    }
    .runWith(Sink.foreach(_.foreach(println)))

}
