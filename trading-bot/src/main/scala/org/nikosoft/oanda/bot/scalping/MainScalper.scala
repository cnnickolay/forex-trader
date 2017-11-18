package org.nikosoft.oanda.bot.scalping

import java.nio.file.Paths
import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString
import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.bot.scalping.tradingmodels.{BigSMATradingModel, BigSMATradingModelWithStopLoss, SingleCandleTrader}
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator, SMACandleCloseIndicator}

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

object MainScalper extends App {

  import Model._

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val stepDays = 2

  //  val startTime = LocalDateTime.now
  //  val duration = Duration.between(LocalDateTime.now, startTime)
  //  println(s"Process took $duration")

  @tailrec
  def fillInDates(date: LocalDateTime, endDate: LocalDateTime, step: Long = 1, allDates: List[LocalDateTime] = Nil): List[LocalDateTime] = {
    val nextDate = date.plusDays(step)
    val _allDates = if (allDates.isEmpty) date +: Nil else allDates
    if (nextDate.isEqual(endDate) || nextDate.isAfter(endDate)) _allDates :+ endDate
    else fillInDates(nextDate, endDate, step, _allDates :+ nextDate)
  }

  val startDate = "2016-01-01T00:00:00Z"
  val endDate = "2016-06-01T00:00:00Z"
  val cardinality = "H1"

  implicit val formats = JsonSerializers.formats

  import org.json4s.jackson.Serialization._

  val eurUsd = InstrumentName("EUR_USD")

  val dates = fillInDates(LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME), LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME), stepDays)

  def url(from: LocalDateTime, to: LocalDateTime, granularity: String) =
    s"/v3/instruments/${eurUsd.value}/candles?from=${from.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
      s"to=${to.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"}&" +
      s"granularity=$granularity&includeFirst=True"

  def oandaSource(chart: Chart, granularity: String) = {
    val range = (dates, dates.tail).zipped
    Source(range
      .map { case (from, to) =>
        val generatedUrl = url(from, to, granularity)
        (from, to, HttpRequest(uri = generatedUrl, headers = List(RawHeader("Authorization", GlobalProperties.OandaToken))))
      }
    )
      .via(Flow[(LocalDateTime, LocalDateTime, HttpRequest)]
        .map { case (from, to, request) =>
          //          println(s"Fetching from $from to $to")
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

  def csvSource(chart: Chart, file: String) = {
    FileIO.fromPath(Paths.get(file))
      .via(
        Framing.delimiter(ByteString("\n"), maximumFrameLength = 999999, allowTruncation = true)
          .map(_.utf8String)
          .map(read[CandlesResponse])
          .mapConcat(_.candles.toList)
          .mapConcat(candle => candle.mid.map(CandleStick.toCandleStick(candle, _)).flatMap(chart.addCandleStick).toList)
      )
  }

  def singleCandleTrader(): Unit = {
    val model = new SingleCandleTrader(20, 100, 80, Some(20), 0) // 2016
    val trader = new Trader(15, model)

    Await.ready(
      csvSource(new Chart(), s"/Users/niko/projects/oanda-trader/eur_usd_raw_2017_H1.csv")
        .via(Flow[CandleStick].map(trader.processCandles))
        .runWith(Sink.ignore), Inf)

    trader.trades.reverse.foldLeft(0) ((profit, trade) => {
      val totalProfit = profit + trade.profitPips
      println(s"Total profit $totalProfit, trade type ${trade.tradeType}, position type ${trade.position.positionType}, profit: ${trade.profitPips}, duration ${trade.duration}, open price ${trade.position.executionPrice}, close price ${trade.closedAtPrice}, order created at ${trade.position.creationOrder.orderCreatedAt.time}, executed at ${trade.position.executionCandle.time}, closed at ${trade.orderClosedAt.time}")
      totalProfit
    })

    println(trader.statsString)
  }

  def singleRun() = {
    val model = new BigSMATradingModel(15, 3, 1000, 24, 168, 20) // 2016
//    val model = new BigSMATradingModel(15,1900,44,400,50) // 2017
    val trader = new Trader(15, model)

    Await.ready(
      csvSource(new Chart(indicators = Seq(new SMACandleCloseIndicator(model.smaRange))), s"/Users/niko/projects/oanda-trader/eur_usd_raw_2017_H1.csv")
        .via(Flow[CandleStick].map(trader.processCandles))
        .runWith(Sink.ignore), Inf)

    trader.trades.reverse.foldLeft(0) ((profit, trade) => {
      val totalProfit = profit + trade.profitPips
      println(s"Total profit $totalProfit, trade type ${trade.tradeType}, position type ${trade.position.positionType}, profit: ${trade.profitPips}, duration ${trade.duration}, open price ${trade.position.executionPrice}, close price ${trade.closedAtPrice}, order created at ${trade.position.creationOrder.orderCreatedAt.time}, executed at ${trade.position.executionCandle.time}, closed at ${trade.orderClosedAt.time}")
      totalProfit
    })

    println(trader.statsString)
  }

  def findBestParams() = {
//    val years = 2016 +: Nil
    val years = 2015 +: 2016 +: 2017 +: Nil

    val candlesByYear: Map[Int, List[CandleStick]] = years.map { year =>
      val exec = csvSource(new Chart(), s"/Users/niko/projects/oanda-trader/eur_usd_raw_${year}_H1.csv").toMat(Sink.seq)(Keep.right).run
      (year, Await.result(exec, Inf).toList)
    }.toMap

    @volatile var totalProcessed = 0

    val allTraderParams = for {
      tipLength <- 100 to 200 by 20
      pipsToActivate <- 100 to 300 by 25
      pipsToTakeProfit <- 20 to 150 by 10
      pipsToStopLoss <- 10 to 150 by 10
//      maxCandlesToGo <- 0 to 20 by 1
//      year <- 2015 to 2017
    } yield (2017, new SingleCandleTrader(tipLength = tipLength, pipsToActivate = pipsToActivate, pipsToTakeProfit = pipsToTakeProfit, pipsToStopLoss = Option(pipsToStopLoss), maxCandlesToGo = 0))

    println(s"Total variations to check ${allTraderParams.length}")
    val startedAt = LocalDateTime.now

/*
    val traderManager = new TraderManager(15, allTraderParams)

    val candles = candlesByYear(2016)
    println(s"Total candles ${candles.length}")
    candles.foreach(traderManager.processCandles)
*/

    allTraderParams.par.map { case (year, model) =>
      val trader = new Trader(15, model)
      candlesByYear(year).foreach { trader.processCandles }
      totalProcessed = totalProcessed + 1
      if (totalProcessed % 1000 == 0) println(totalProcessed)
      val traderStats = trader.stats
      (year, model, traderStats._1, traderStats)
    }.toList
      .groupBy(_._1)
      .foreach { case (year, params) =>
        println(s"=========== $year ===============")
        val sorted = params.sortBy(_._3).reverse
        (sorted.take(2) ++ sorted.takeRight(2)).foreach { case (_, param, profit, stats) =>
          println(param)
          println(s"Profit $profit")
          println(s"Profit $stats")
          println("-----------------")
        }
      }

    println(s"Total duration ${Duration.between(startedAt, LocalDateTime.now).toString}")
  }

//  singleRun()
  singleCandleTrader()
//  findBestParams()

  Await.ready(actorSystem.terminate(), Inf)
}
