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
import org.nikosoft.oanda.instruments.Model.{ATRCandleIndicator, CMOCandleCloseIndicator, CandleStick, Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator, RSICandleCloseIndicator, SMACandleCloseIndicator, StochasticCandleIndicator}
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.math.BigDecimal.RoundingMode.HALF_UP
import scala.util.Try

object CandleStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val stepDays = 2

  val startTime = LocalDateTime.now
  //  storeData("M1")
//    storeData("2016-01-01T00:00:00Z", "2016-01-05T00:00:00Z", "M5", "train")
  storeData("2016-01-01T00:00:00Z", "2016-03-01T00:00:00Z", "M5", "raw")
//  storeData("2016-06-01T00:00:00Z", "2016-08-01T00:00:00Z", "M5", "test")

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

  def storeData(startDate: String, endDate: String, cardinality: String, filePrefix: String) = {
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

    val sink = FileIO.toPath(Paths.get(s"eur_usd_${filePrefix}_$cardinality.csv"))

    val indicators = Seq(
      new RSICandleCloseIndicator(14),
      new SMACandleCloseIndicator(9),
      new EMACandleCloseIndicator(21),
      new EMACandleCloseIndicator(50),
      //      new CMOCandleCloseIndicator(21)
      new MACDCandleCloseIndicator()
      //      new ATRCandleIndicator(14),
      //      new StochasticCandleIndicator(5, Some(3), Some(3))
    )

    val roundTo = 5

    def extractCsv(c: CandleStick): List[List[String]] = Try {
      val list: List[String] = (
        List[BigDecimal](c.open, c.high, c.low, c.close, c.volume) ++
        List[BigDecimal](c.indicators("RSICandleCloseIndicator_14").asInstanceOf[BigDecimal],
          c.indicators("SMACandleCloseIndicator_9").asInstanceOf[BigDecimal],
          c.indicators("EMACandleCloseIndicator_21").asInstanceOf[BigDecimal],
          c.indicators("EMACandleCloseIndicator_50").asInstanceOf[BigDecimal]
        ) ++ c.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram)
        .map((value: BigDecimal) => value.setScale(roundTo, HALF_UP).toString)
      List(c.time.toString) ++ list
    }.toOption.toList

    val exec = source(new Chart(indicators = indicators), cardinality)
      .via(Flow[CandleStick]
        .mapConcat(extractCsv)
        .map(_.mkString(",") + '\n')
        .map(ByteString(_))
      )
      .toMat(sink)(Keep.right)
      .run()

    Await.ready(exec, Inf)
  }

}
