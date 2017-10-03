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
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object PriceStreamer extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val accountId = AccountID(GlobalProperties.TradingAccountId)
  val eurUsd = InstrumentName("EUR_USD")
  val usdGbp = InstrumentName("GBP_USD")

  def params(instrumentName: InstrumentName) = Seq(
    Option(s"instruments=${Seq(instrumentName).map(_.value).mkString(",")}"),
    Option(s"smooth=False")
  ).flatten.mkString("&")
  def url(instrumentName: InstrumentName) = s"/v3/accounts/${accountId.value}/pricing/stream?${params(instrumentName)}"

  val eurUsdResponse = Http().singleRequest(HttpRequest(
    uri = s"https://stream-fxtrade.oanda.com${url(eurUsd)}",
    headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))))

  val gbpUsdResponse = Http().singleRequest(HttpRequest(
    uri = s"https://stream-fxtrade.oanda.com${url(usdGbp)}",
    headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))))

  for {
    eurUsd <- eurUsdResponse
    gbpUsd <- gbpUsdResponse
  } yield {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val source1 = eurUsd.entity.dataBytes
      val source2 = gbpUsd.entity.dataBytes

      val merge = builder.add(Merge[ByteString](2))

      val flow = Framing.delimiter(ByteString("\n"), maximumFrameLength = 99999, allowTruncation = true).map(bs => bs.utf8String)

      source1 ~> merge ~> flow ~> Sink.foreach[String](println)
      source2 ~> merge

      ClosedShape
    }).run()
  }

}
