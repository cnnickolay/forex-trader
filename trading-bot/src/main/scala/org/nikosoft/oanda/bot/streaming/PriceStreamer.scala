package org.nikosoft.oanda.bot.streaming

import akka.NotUsed
import akka.actor.ActorSystem
import akka.dispatch.Futures
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpCharsets, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.javadsl.FramingTruncation
import akka.stream.scaladsl.{Flow, Framing, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.util.ByteString
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.json4s.jackson.Serialization.read
import org.nikosoft.oanda.api.ApiModel.PricingModel.Price
import org.nikosoft.oanda.api.JsonSerializers

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object PriceStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  implicit val formats = JsonSerializers.formats

  val accountId = AccountID(GlobalProperties.TradingAccountId)
  val eurUsd = InstrumentName("EUR_USD")

  def params(instrumentName: InstrumentName) = Seq(
    Option(s"instruments=${Seq(instrumentName).map(_.value).mkString(",")}"),
    Option(s"smooth=False")
  ).flatten.mkString("&")
  def url(instrumentName: InstrumentName) = s"/v3/accounts/${accountId.value}/pricing/stream?${params(instrumentName)}"

  val eurUsdResponse = Http().singleRequest(HttpRequest(
    uri = s"https://stream-fxtrade.oanda.com${url(eurUsd)}",
    headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))))

  var prices = List.empty[Price]

  for {
    eurUsd <- eurUsdResponse
  } yield {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val source1 = eurUsd.entity.dataBytes
      val flow = Framing
        .delimiter(ByteString("\n"), maximumFrameLength = 99999, allowTruncation = true)
        .map(bs => bs.utf8String)
        .filterNot(_.contains("HEARTBEAT"))
        .map(read[Price])

      source1 ~> flow ~> Sink.foreach[Price](price => {
        prices = price +: prices
        calculateSlope()
      })

      ClosedShape
    }).run()
  }

  def calculateSlope(): Unit = {
    val lastPrice = prices.head
    val fromPrice = lastPrice.time.minusSeconds(60*5)
    val pairs = prices.filter(_.time.isAfter(fromPrice)).map(price => {
      (price.time.getMillis - fromPrice.toInstant.getMillis, price.closeoutAsk.value.toDouble)
    }).reverse
    val first = pairs.head._2
    val normalizedPairs = pairs.map { case (x, y) => (x, ((y - first) * 100000).toInt)}
    val reg = new SimpleRegression(false)
    normalizedPairs.foreach(pair => {
      reg.addData(pair._1.toDouble, pair._2)
    })
    println(s"Slope ${reg.getSlope}, ${normalizedPairs.take(3)}...${normalizedPairs.takeRight(3)}")
  }

}
