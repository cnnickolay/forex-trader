package org.nikosoft.oanda.bot.streaming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.util.Success

object InvestingComStreamer {

  type Advices = List[(String, Any)]

  val SummaryTechnical = "summary_technical"
  val SummaryAverages = "summary_averages"

  val anchors = List("RSI(14)", "STOCH(9,6)", "STOCHRSI(14)", "MACD(12,26)", "ADX(14)", "Williams %R", "CCI(14)", "ATR(14)", "Highs/Lows(14)", "Ultimate Oscillator", "ROC", "Bull/Bear Power(13)")
  val averages = List("MA200", "MA100", "MA50", "MA20", "MA10", "MA5")
  val pivots = List("Classic", "Fibonacci", "Camarilla", "Woodie's", "DeMark's")

  def advisorSource(period: Int)(implicit materializer: ActorMaterializer, system: ActorSystem) = {
    val request = HttpRequest(uri = "/instruments/Service/GetTechincalData",
      method = HttpMethods.POST,
      entity = FormData(Map("pairID" -> "1", "period" -> period.toString, "viewType" -> "normal")).toEntity,
      headers = List(
        RawHeader("Origin", "https://www.investing.com"),
        RawHeader("Accept-Language", "en-US,en;q=0.8,ru;q=0.6,fr;q=0.4"),
        RawHeader("Accept", "*/*"),
        RawHeader("Referer", "https://www.investing.com/currencies/eur-usd-technical"),
        RawHeader("X-Requested-With", "XMLHttpRequest")
      )
    )

    Flow[String].map(_ => request -> 1)
      .via(Http().cachedHostConnectionPoolHttps("www.investing.com"))
      .collect { case (Success(response), _) => response }
      .flatMapConcat(_.entity.dataBytes.map(_.utf8String))
      .map { page =>
        val extractor = """.*>(.+?)<.*""".r
        val extractOverall = """.*(Neutral|Sell|Buy)\:.*>(\d+)<.*""".r
        val extractSummary = """\s+Summary.*>(.*?)<.*""".r
        val extractAverage = """\s+(\w+)\s+.*""".r
        val extractNumber = """.*>(\d+\.\d+)<.*""".r
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
            val pivotPoints = pivots.find(lines.head.contains).map(found => {
              found -> lines.tail.take(7).collect { case extractNumber(number) => number.toDouble }
            })
            val summary = if (lines.last.contains("Summary:")) {
              Map((if (!result.exists(_._1 == SummaryTechnical)) SummaryTechnical else SummaryAverages) -> {
                val extractSummary(summary) = lines.last
                ("Summary" -> summary) +: lines.collect { case extractOverall(sentiment, votes) => sentiment -> votes.toInt }
              })
            } else Map.empty[String, Any]
            result ++ anchor ++ summary ++ averageSimple ++ averageExponential ++ pivotPoints
          })
      }
  }

}
