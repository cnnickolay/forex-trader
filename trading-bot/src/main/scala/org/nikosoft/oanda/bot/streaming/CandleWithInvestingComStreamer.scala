package org.nikosoft.oanda.bot.streaming

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, Framing, GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.util.ByteString
import org.json4s.jackson.Serialization.read
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.bot.streaming.InvestingComStreamer.{Advices, SummaryAverages, SummaryTechnical, advisorSource}
import org.nikosoft.oanda.instruments.Model.CandleStick

import scala.concurrent.duration.DurationDouble
import scala.util.{Failure, Success}
import org.json4s.jackson.Serialization._

object CandleWithInvestingComStreamer extends App {

  implicit val formats = JsonSerializers.formats

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  def url = s"/v3/instruments/${InstrumentName("EUR_USD").value}/candles?" +
//    s"from=${LocalDateTime.now.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
//    s"to=${LocalDateTime.now.plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
    s"count=1&granularity=M1"

  val flow: Flow[String, CandleStick, NotUsed] = Flow[String].map(_ => HttpRequest(uri = url, headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))) -> 1)
    .via(Http().cachedHostConnectionPoolHttps("api-fxtrade.oanda.com"))
    .collect { case (Success(response), _) => response }
    .flatMapConcat(_.entity.dataBytes.via(
      Framing.delimiter(ByteString("\n"), maximumFrameLength = 999999, allowTruncation = true)
        .map(_.utf8String)
        .map(read[CandlesResponse])
        .mapConcat(_.candles.toList)))
    .mapConcat(candle => candle.mid.map(CandleStick.toCandleStick(candle, _)).toList)

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zip = builder.add(ZipWith[CandleStick, Advices, Advices, Advices, Advices, Advices, (CandleStick, Advices, Advices, Advices, Advices, Advices)]((a, b, c, d, e, f) => (a, b, c, d, e, f)))
    val tick = Source.tick(0.minutes, 1.minute, "_")
    val broadcast = builder.add(Broadcast[String](6))

    val printlnSink = Sink.foreach[(CandleStick, Advices, Advices, Advices, Advices, Advices)](a => {
      println(s"${a._1}")
      println(s"${a._2.find(_._1 == SummaryAverages).get} - ${a._2.find(_._1 == SummaryTechnical).get}")
      println(s"${a._3.find(_._1 == SummaryAverages).get} - ${a._3.find(_._1 == SummaryTechnical).get}")
      println(s"${a._4.find(_._1 == SummaryAverages).get} - ${a._4.find(_._1 == SummaryTechnical).get}")
      println(s"${a._5.find(_._1 == SummaryAverages).get} - ${a._5.find(_._1 == SummaryTechnical).get}")
      println(s"${a._6.find(_._1 == SummaryAverages).get} - ${a._6.find(_._1 == SummaryTechnical).get}")
      println("-----")
    })

    // @formatter:off
    tick ~> broadcast
            broadcast ~> flow                ~> zip.in0
            broadcast ~> advisorSource(60)   ~> zip.in1
            broadcast ~> advisorSource(300)  ~> zip.in2
            broadcast ~> advisorSource(900)  ~> zip.in3
            broadcast ~> advisorSource(1800) ~> zip.in4
            broadcast ~> advisorSource(3600) ~> zip.in5
                                                zip.out ~> printlnSink
    // @formatter:on

    ClosedShape
  }).run()


}
