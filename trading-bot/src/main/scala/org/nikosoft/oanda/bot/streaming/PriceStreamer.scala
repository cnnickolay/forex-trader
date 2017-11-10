package org.nikosoft.oanda.bot.streaming

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Broadcast, Flow, Framing, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.util.ByteString
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PricingModel.Price
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers

import scala.util.Success

object PriceStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  implicit val formats = JsonSerializers.formats

  val producerSettings = ProducerSettings(actorSystem, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val accountId = AccountID(GlobalProperties.TradingAccountId)
  val eurUsd = InstrumentName("EUR_USD")

  def params(instrumentName: InstrumentName) = Seq(
    Option(s"instruments=${Seq(instrumentName).map(_.value).mkString(",")}"),
    Option(s"smooth=False")
  ).flatten.mkString("&")

  def url(instrumentName: InstrumentName) = s"/v3/accounts/${accountId.value}/pricing/stream?${params(instrumentName)}"

  val request = HttpRequest(uri = url(eurUsd), headers = List(RawHeader("Authorization", GlobalProperties.OandaToken)))

  val source = Source
    .single(request -> 1)
    .via(Http().cachedHostConnectionPoolHttps("stream-fxtrade.oanda.com"))
    .collect { case (Success(response), _) => response }
    .flatMapConcat(_.entity.dataBytes)

  var prices = List.empty[Price]

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[String](2))

    val flow: Flow[ByteString, String, NotUsed] = Framing
      .delimiter(ByteString("\n"), maximumFrameLength = 99999, allowTruncation = true)
      .map(bs => bs.utf8String)
      .filterNot(_.contains("HEARTBEAT"))

    val producerFlow = Flow[String].map(json => new ProducerRecord[Array[Byte], String]("prices", json))

    source ~> flow ~> broadcast
    broadcast ~> producerFlow ~> Producer.plainSink(producerSettings)
    broadcast ~> Sink.foreach(println)
    ClosedShape
  }).run()

}
