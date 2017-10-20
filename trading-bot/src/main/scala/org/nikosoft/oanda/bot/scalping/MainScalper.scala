package org.nikosoft.oanda.bot.scalping

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, MACDCandleCloseIndicator}

import scala.annotation.tailrec

object MainScalper extends App {

  import TraderModel._

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val stepDays = 2

  //  val startTime = LocalDateTime.now
  //  val duration = Duration.between(LocalDateTime.now, startTime)
  //  println(s"Process took $duration")

  @tailrec
  def fillInDates(date: LocalDateTime, endDate: LocalDateTime, step: Long = 1, allDates: List[LocalDateTime] = Nil): List[LocalDateTime] = {
    val nextDate = date.plusDays(step)
    val _allDates = if (allDates.isEmpty) date +: Nil else allDates
    if (nextDate.isEqual(endDate) || nextDate.isAfter(endDate)) _allDates :+ endDate
    else fillInDates(nextDate, endDate, step, _allDates :+ nextDate)
  }

  val startDate = "2016-01-01T00:00:00Z"
  val endDate = "2016-06-01T00:00:00Z"
  val cardinality = "M5"

  implicit val formats = JsonSerializers.formats

  import org.json4s.jackson.Serialization._

  val eurUsd = InstrumentName("EUR_USD")

  val dates = fillInDates(LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME), LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME), stepDays)

  def url(from: LocalDateTime, to: LocalDateTime, granularity: String) =
    s"/v3/instruments/${eurUsd.value}/candles?from=${from.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
      s"to=${to.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
      s"granularity=$granularity&includeFirst=True"

  def source(chart: Chart, granularity: String): Source[CandleStick, NotUsed] = {
    val range = (dates, dates.tail).zipped
    Source(range
      .map { case (from, to) =>
        val generatedUrl = url(from, to, granularity)
        (from, to, HttpRequest(uri = generatedUrl, headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))))
      }
    )
      .via(Flow[(LocalDateTime, LocalDateTime, HttpRequest)]
        .map { case (from, to, request) =>
//          println(s"Fetching from $from to $to")
          request
        })
      .via(Http().outgoingConnectionHttps("api-fxtrade.oanda.com"))
      .flatMapConcat(_.entity.dataBytes.via(
        Framing.delimiter(ByteString("\n"), maximumFrameLength = 999999, allowTruncation = true)
          .map(_.utf8String)
          .map(read[CandlesResponse])
          .mapConcat(_.candles.toList)))
      .mapConcat(candle => candle.mid.map(CandleStick.toCandleStick(candle, _)).flatMap(chart.addCandleStick).toList)
  }

  val indicators = Seq(
    new MACDCandleCloseIndicator()
  )

  val roundTo = 5

  val trader = new Trader(commission = 15, takeProfit = 50, stopLoss = -50)

  val exec = source(new Chart(indicators = indicators), cardinality)
    .via(Flow[CandleStick].sliding(3, 1).mapConcat(candles => trader.processCandles(candles).toList))
    .runWith(Sink.foreach[Trade](trade => {
      println(s"${trader.stats}, profit from last trade: ${trade.profitPips}, duration ${trade.duration}, open at ${trade.openCandle.time}, closed at ${trade.closeCandle.time}")
    }))

}
