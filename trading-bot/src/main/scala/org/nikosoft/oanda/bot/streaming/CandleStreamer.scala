package org.nikosoft.oanda.bot.streaming

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, RSICandleCloseIndicator, StochasticCandleIndicator}

object CandleStreamer extends App {

  implicit val formats = JsonSerializers.formats

  import org.json4s.native.Serialization._

  val decider: Supervision.Decider = { e =>
    e.printStackTrace()
    Supervision.Stop
  }

  implicit val actorSystem = ActorSystem("streamer")
  val strategy = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(strategy)

  val accountId = AccountID(GlobalProperties.TradingAccountId)
  val eurUsd = InstrumentName("EUR_USD")
  val usdGbp = InstrumentName("GBP_USD")

  val startDate = LocalDateTime.parse("2016-06-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)

  def url(from: LocalDateTime, to: LocalDateTime, granularity: String) = s"/v3/instruments/${eurUsd.value}/candles?from=${from.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
    s"to=${to.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
    s"granularity=$granularity&includeFirst=True"

  def source(chart: Chart, granularity: String): Source[CandleStick, NotUsed] =
    Source(Stream.from(0)
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

  source(new Chart(), "M30")
    .runWith(Sink.foreach[CandleStick](println))

}
