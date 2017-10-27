package org.nikosoft.oanda.bot.scalping

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator}

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

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

  def oandaSource(chart: Chart, granularity: String) = {
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

  def csvSource(chart: Chart, file: String) = {
    FileIO.fromPath(Paths.get(file))
      .via(
        Framing.delimiter(ByteString("\n"), maximumFrameLength = 999999, allowTruncation = true)
          .map(_.utf8String)
          .map(read[CandlesResponse])
          .mapConcat(_.candles.toList)
          .mapConcat(candle => candle.mid.map(CandleStick.toCandleStick(candle, _)).flatMap(chart.addCandleStick).toList)
      )
  }

  val indicators = Seq(
    new MACDCandleCloseIndicator(),
    new EMACandleCloseIndicator(20),
    new EMACandleCloseIndicator(30),
    new EMACandleCloseIndicator(50)
  )

  val roundTo = 5

  val trader = new Trader(commission = 10, openOrderOffset = 0, takeProfit = 40, stopLoss = 40)

  val exec = csvSource(new Chart(indicators = indicators), "/Users/niko/projects/oanda-trader/eur_usd_raw_2017_M5.csv")
    .via(Flow[CandleStick].sliding(4, 1).mapConcat(candles => trader.processCandles(candles).toList))
    .runWith(Sink.foreach[Order] {
      case order if order.orderState == PendingOrder => println(s"Opening ${order.orderType}, buy at ${order.openAtPrice}, current close price ${order.createdAtCandle.close},  created at ${order.createdAtCandle.time}, take profit ${order.takeProfit}, stop loss ${order.stopLoss}")
      case order@Order(_, _, _, _, _, _, closedAtPrice, Some(boughtAt), Some(closedAt), orderState@(TakeProfitOrder | StopLossOrder | CancelledOrder)) =>
        println(s"State $orderState, type ${order.orderType}, profit: ${order.profitPips}, duration ${order.duration}, open price ${order.openAtPrice}, stop loss ${order.stopLoss}, take profit ${order.takeProfit}, close price $closedAtPrice, open at ${boughtAt.time}, closed at ${closedAt.time}")
        println(">> " + trader.stats)
      case _ =>
    })

  Await.ready(exec, Inf)
  Await.ready(actorSystem.terminate(), Inf)
}
