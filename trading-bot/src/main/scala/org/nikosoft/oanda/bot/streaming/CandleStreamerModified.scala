package org.nikosoft.oanda.bot.streaming

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Broadcast, Flow, Framing, GraphDSL, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, ClosedShape, Supervision}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.Candlestick
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.bot.ml.BalancedZip
import org.nikosoft.oanda.instruments.Model.{ATRCandleIndicator, CMOCandleCloseIndicator, CandleStick, Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator, RSICandleCloseIndicator, StochasticCandleIndicator}

import scala.concurrent.Future

object CandleStreamerModified extends App {

  implicit val formats = JsonSerializers.formats

  import org.json4s.native.Serialization._

  val decider: Supervision.Decider = { e =>
    e.printStackTrace()
    Supervision.Stop
  }

  implicit val actorSystem = ActorSystem("streamer")
  val strategy = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(strategy)

  val eurUsd = InstrumentName("EUR_USD")

  val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = Http().outgoingConnectionHttps("api-fxtrade.oanda.com")

  val flow =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 999999, allowTruncation = true)
      .map(bs => bs.utf8String)
      .map(read[CandlesResponse])
      .mapConcat(_.candles.toList)

  def url(from: LocalDateTime, to: LocalDateTime, granularity: String) = s"/v3/instruments/${eurUsd.value}/candles?from=${from.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
    s"to=${to.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
    s"granularity=$granularity"

  val startDate = LocalDateTime.parse("2016-06-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)

  val headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))

  def source(chart: Chart, granularity: String): Source[CandleStick, NotUsed] =
    Source((0 to 10)
      .map(offset => url(startDate.plusDays(offset), startDate.plusDays(offset + 1), granularity))
      .map(uri => HttpRequest(uri = uri, headers = headers))
    )
      .via(connectionFlow)
      .flatMapConcat(_.entity.dataBytes.via(flow))
      .mapConcat(candle => candle.mid.map(CandleStick.toCandleStick(candle, _)).flatMap(chart.addCandleStick).toList)

  val indicators = Seq(
//    new MACDCandleCloseIndicator(),
    new RSICandleCloseIndicator(14),
//    new EMACandleCloseIndicator(50),
//    new EMACandleCloseIndicator(100),
//    new ATRCandleIndicator(14),
//    new CMOCandleCloseIndicator(21),
    new StochasticCandleIndicator(5, Some(3), Some(3))
  )

  val sink = Sink.foreach[Seq[(CandleStick, CandleStick)]](c => /*println(c.map{case(a, b) => (a.time, b.time)})*/{})
  val sinkLog = Sink.foreach[(CandleStick, CandleStick)] { case (a, b) => println(s"${a.time} / ${b.time}") }

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceA = source(new Chart(indicators = indicators), "M1")
    val sourceB = source(new Chart(indicators = indicators), "M5")

    val zip = builder.add(new BalancedZip[CandleStick]((a, b) => a.time.isAfter(b.time) || a.time.isEqual(b.time)))
    val broadcast = builder.add(Broadcast[(CandleStick, CandleStick)](2))

    sourceA ~> zip.in0
    sourceB ~> zip.in1
               zip.out ~> broadcast
                          broadcast ~> Flow[(CandleStick, CandleStick)].sliding(10) ~> sink
                          broadcast ~> sinkLog

    ClosedShape
  }).run()

}
