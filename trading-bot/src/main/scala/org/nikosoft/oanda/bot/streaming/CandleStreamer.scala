package org.nikosoft.oanda.bot.streaming

import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Source}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.instruments.Model.{ATRCandleIndicator, CMOCandleCloseIndicator, CandleStick, Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator, RSICandleCloseIndicator, StochasticCandleIndicator}
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.math.BigDecimal.RoundingMode.HALF_UP

object CandleStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val startDate = LocalDateTime.parse("2016-01-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
  val endDate = LocalDateTime.parse("2017-01-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
  val stepDays = 2

  val startTime = LocalDateTime.now
//  storeData("M1")
  storeData("M5")
//  storeData("M10")
//  storeData("H1")

  val duration = Duration.between(LocalDateTime.now, startTime)
  println(s"Process took $duration")

  Await.ready(actorSystem.terminate(), Inf)

  @tailrec
  def fillInDates(date: LocalDateTime, endDate: LocalDateTime, step: Long = 1, allDates: List[LocalDateTime] = Nil): List[LocalDateTime] = {
    val nextDate = date.plusDays(step)
    val _allDates = if (allDates.isEmpty) date +: Nil else allDates
    if (nextDate.isEqual(endDate) || nextDate.isAfter(endDate)) _allDates :+ endDate
    else fillInDates(nextDate, endDate, step, _allDates :+ nextDate)
  }

  def storeData(cardinality: String) = {
    implicit val formats = JsonSerializers.formats

    import org.json4s.jackson.Serialization._
    val eurUsd = InstrumentName("EUR_USD")

    val dates = fillInDates(startDate, endDate, stepDays)

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
            println(s"Fetching from $from to $to")
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

    val sink = FileIO.toPath(Paths.get(s"eur_usd_$cardinality.csv"))

    val indicators = Seq(
//      new RSICandleCloseIndicator(14),
//      new EMACandleCloseIndicator(50),
//      new EMACandleCloseIndicator(100),
//      new ATRCandleIndicator(14),
      new CMOCandleCloseIndicator(21)
//      new StochasticCandleIndicator(5, Some(3), Some(3)),
//      new MACDCandleCloseIndicator()
    )

    def extractCsv(c: CandleStick) = {
      val roundTo = 10
      List(c.time, c.open, c.high, c.low, c.close, c.volume) ++ c.indicators.flatMap {
        case (_, macd: MACDItem) => macd.histogram ++ macd.macd ++ Nil
        case (_, value: Double) => value +: Nil
        case (_, value: BigDecimal) => value +: Nil
      }.map {
        case value: Double => BigDecimal(value).setScale(roundTo, HALF_UP).toString
        case value: BigDecimal => value.setScale(roundTo, HALF_UP).toString
        case value => value.toString
      }
    }

    val exec = source(new Chart(indicators = indicators), cardinality)
      .via(Flow[CandleStick]
        .map(extractCsv)
        .map(_.mkString(",") + '\n')
        .map(ByteString(_))
      )
      .toMat(sink)(Keep.right)
      .run()

    Await.ready(exec, Inf)
  }

}
