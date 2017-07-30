package org.nikosoft.oanda.bot.streaming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.{Flow, Framing, GraphDSL, Merge, RunnableGraph, Sink}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.util.ByteString
import org.nikosoft.oanda.api.{Api, JsonSerializers}
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object CandleStreamer extends App {

  implicit val formats = JsonSerializers.formats
  import org.json4s.native.Serialization._

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val accountId = AccountID("001-004-1442547-003")
  val eurUsd = InstrumentName("EUR_USD")
  val usdGbp = InstrumentName("GBP_USD")

  val gbpUsdResponseFuture = Http().singleRequest(HttpRequest(
    uri = s"https://api-fxtrade.oanda.com/v3/instruments/${eurUsd.value}/candles?count=20",
    headers = List(RawHeader("Authorization", "Bearer e5f024cbdd6458cfb0f1f196fe7b7295-1b5f5139c3b00e9b90a166c1cb1d4095"))))

  val response = Await.result(gbpUsdResponseFuture, Duration.Inf)

  val flow =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 99999, allowTruncation = true)
      .map(bs => bs.utf8String)
      .map(read[CandlesResponse])
      .mapConcat(_.candles.toList)

  response.entity.dataBytes
    .via(flow)
    .runWith(Sink.foreach(println))

}
