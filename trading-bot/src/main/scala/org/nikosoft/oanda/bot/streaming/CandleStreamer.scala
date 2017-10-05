package org.nikosoft.oanda.bot.streaming

import java.nio.file.Paths
import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.instruments.Model.{ATRCandleIndicator, CMOCandleCloseIndicator, CandleStick, Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator, RSICandleCloseIndicator, StochasticCandleIndicator}

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

object CandleStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val daysToStore = 30

  val startTime = LocalDateTime.now
  storeData("S30")
  storeData("M5")
  storeData("M10")
  storeData("H1")

  val duration = Duration.between(LocalDateTime.now, startTime)
  println(s"Process took $duration")

  Await.ready(actorSystem.terminate(), Inf)

  def storeData(cardinality: String) = {
    implicit val formats = JsonSerializers.formats

    import org.json4s.native.Serialization._

    val eurUsd = InstrumentName("EUR_USD")

    val startDate = LocalDateTime.parse("2016-06-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)

    def url(from: LocalDateTime, to: LocalDateTime, granularity: String) = s"/v3/instruments/${eurUsd.value}/candles?from=${from.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
      s"to=${to.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
      s"granularity=$granularity&includeFirst=True"

    def source(chart: Chart, granularity: String): Source[CandleStick, NotUsed] =
      Source((0 until daysToStore)
        .map(offset => url(startDate.plusDays(offset), startDate.plusDays(offset + 1), granularity))
        .map(uri => HttpRequest(uri = uri, headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))))
      )
        .via(Http().outgoingConnectionHttps("api-fxtrade.oanda.com"))
        .flatMapConcat(_.entity.dataBytes.via(
          Framing.delimiter(ByteString("\n"), maximumFrameLength = 999999, allowTruncation = true)
            .map(_.utf8String)
            .map(read[CandlesResponse])
            .mapConcat(_.candles.toList)))
        .mapConcat(candle => candle.mid.map(CandleStick.toCandleStick(candle, _)).flatMap(chart.addCandleStick).toList)

    val sink = FileIO.toPath(Paths.get(s"target/eur_usd_$cardinality.csv"))

    val indicators = Seq(
      new RSICandleCloseIndicator(14),
      new EMACandleCloseIndicator(50),
      new EMACandleCloseIndicator(100),
      new ATRCandleIndicator(14),
      new CMOCandleCloseIndicator(21),
      new StochasticCandleIndicator(5, Some(3), Some(3)),
      new MACDCandleCloseIndicator()
    )

    val exec = source(new Chart(indicators = indicators), cardinality)
      .via(Flow[CandleStick]
        .map(c => CandleStick.unapply(c)
          .map(_.productIterator)
          .map(tuple => tuple.mkString(",") + '\n')
          .map(ByteString(_))
        )
      )
      .mapConcat(s => s.toList)
      .toMat(sink)(Keep.right)
      .run()

    Await.ready(exec, Inf)
  }

}
