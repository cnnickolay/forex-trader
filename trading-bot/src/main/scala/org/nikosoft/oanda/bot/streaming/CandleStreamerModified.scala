package org.nikosoft.oanda.bot.streaming

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Framing, GraphDSL, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, ClosedShape, Supervision}
import akka.util.ByteString
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.Candlestick
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse

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

  val headers = List(RawHeader("Authorization", "Bearer e5f024cbdd6458cfb0f1f196fe7b7295-1b5f5139c3b00e9b90a166c1cb1d4095"))

  def source(granularity: String, multiplication: Int): Source[Candlestick, NotUsed] =
    Source((0 to 10)
      .map(offset => url(startDate.plusDays(offset), startDate.plusDays(offset + 1), granularity))
      .map(uri => HttpRequest(uri = uri, headers = headers))
    )
      .via(connectionFlow)
      .flatMapConcat(_.entity.dataBytes.via(flow))
      .mapConcat(candle => (0 until multiplication).map(_ => candle))

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceH1 = source("M5", 1)
    val sourceH6 = source("M10", 2)

    val zip = builder.add(Zip[Candlestick, Candlestick]())

    sourceH1 ~> zip.in0
    sourceH6 ~> zip.in1
                zip.out ~> Sink.foreach[(Candlestick, Candlestick)]{ case (c1, c2) => println(s"${c1.time} / ${c2.time}") }

    ClosedShape
  }).run()

}
