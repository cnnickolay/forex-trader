package org.nikosoft.oanda.bot.scalpers

import akka.actor.{Actor, PoisonPill}
import org.joda.time.Instant
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.OrderModel.{MarketOrderRequest, TimeInForce}
import org.nikosoft.oanda.api.ApiModel.PositionModel.Position
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PriceValue}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TransactionModel.StopLossDetails
import org.nikosoft.oanda.api.`def`.OrderApi.CreateOrderRequest
import org.nikosoft.oanda.api.`def`.PositionApi.ClosePositionRequest
import org.nikosoft.oanda.instruments.Model._
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.math.BigDecimal.RoundingMode._
import scalaz.Scalaz._
import scalaz.\/-

object MACDScalperActor {

  trait OpenPosition

  case object OpenShortPosition extends OpenPosition

  case object OpenLongPosition extends OpenPosition

  case object DoNothing extends OpenPosition

  case class TrainingTrade(long: Boolean, entry: CandleStick, exit: CandleStick, profit: Int)
  case class TrainingStatistics(profitPips: Int = 0,
                                lossPips: Int = 0,
                                openedLong: Option[CandleStick] = None,
                                openedShort: Option[CandleStick] = None,
                                trades: Seq[TrainingTrade] = Seq.empty)

}

class MACDScalperActor(chart: Chart) extends Actor {

  import MACDScalperActor._

  var allCandles: Seq[CandleStick] = Seq.empty
  var positionOption: Option[Position] = None
  val tradingUnits = 100
  var currentSpread: Int = _
  val maxSpread = 15
  private val stochasticSettings = "14_3"

  override def preStart(): Unit = {
    updatePosition()
    positionOption.foreach(println)
  }

  def receive: Receive = {
    case candles: Seq[CandleStick] => processCandleSeq(candles)
    case candle: CandleStick => processCandle(candle)
    case price: Price => processPrice(price)

  }

  private def processCandleSeq(candles: Seq[CandleStick]): Unit = {
    val (_, stats) = candles.reverse.foldLeft((Seq.empty[CandleStick], TrainingStatistics())) {
      case ((historicalCandles, _stats@TrainingStatistics(_, _, None, None, _)), candle) =>
        val toppedUpHistoricalCandles = candle +: historicalCandles
        considerOpeningPosition(toppedUpHistoricalCandles) match {
          case Some(OpenLongPosition) => (toppedUpHistoricalCandles, _stats.copy(openedLong = Some(candle)))
          case Some(OpenShortPosition) => (toppedUpHistoricalCandles, _stats.copy(openedShort = Some(candle)))
          case _ => (toppedUpHistoricalCandles, _stats)
        }

      case ((historicalCandles, _stats@TrainingStatistics(_, _, longPosition, shortPosition, _)), exitCandle) =>
        val toppedUpHistoricalCandles = exitCandle +: historicalCandles
        val gainLoss = (longPosition, shortPosition) match {
          case (Some(_openedAt), None) => (exitCandle.close.abs - _openedAt.close.abs).toPips
          case (None, Some(_openedAt)) => (exitCandle.close.abs - _openedAt.close.abs).toPips * -1
        }

        if (considerClosingPosition(toppedUpHistoricalCandles, gainLoss, longPosition.isDefined)) {
          val (trade, _) = (longPosition, shortPosition) match {
            case (Some(_openedAt), None) => (((exitCandle.close - _openedAt.close) * 100000).toInt, _openedAt)
            case (None, Some(_openedAt)) => (((_openedAt.close - exitCandle.close) * 100000).toInt, _openedAt)
          }

          val profitPips = (trade > 0) ? (_stats.profitPips + trade) | _stats.profitPips
          val lossPips = (trade < 0) ? (_stats.lossPips + (-trade)) | _stats.lossPips
          val entry = _stats.openedLong.orElse(_stats.openedShort).get.copy(indicators = Map.empty)
          val exit = exitCandle.copy(indicators = Map.empty)
          val isLong = _stats.openedLong.isDefined
          (
            toppedUpHistoricalCandles,
            _stats.copy(profitPips = profitPips, lossPips = lossPips, openedLong = None, openedShort = None,
              trades = TrainingTrade(
                isLong, entry, exit,
                isLong ? (exit.close.abs - entry.close.abs).toPips | (-1 * (exit.close.abs - entry.close.abs)).toPips) +: _stats.trades)
            )
        } else (toppedUpHistoricalCandles, _stats)
    }

    val avgFee = 13
    val aggregated = stats.trades
      .map(trade => (trade.long ? "long" | "short", trade.entry.time, trade.entry.close, trade.exit.time, trade.exit.close, trade.profit))
    aggregated.foreach(println)
    val grouped = aggregated.map { case (_, time, _, _, _, profit) => (s"${time.toDateTime.getYear}/${time.toDateTime.getMonthOfYear}", profit) }.groupBy(_._1).mapValues(_.map(_._2).sum).toList.sortBy(_._1)
    grouped.foreach(println)
    val totalLoss = aggregated.collect { case (_, _, _, _, _, profit) if profit < 0 => profit }.sum
    val totalProfit = aggregated.collect { case (_, _, _, _, _, profit) if profit > 0 => profit }.sum
    val total = totalProfit + totalLoss
    val totalFees = avgFee * aggregated.size
    println(s"Total trades ${aggregated.size}, total profit $totalProfit, total loss $totalLoss, result $total, fees $totalFees, grand total ${total - totalFees}")
    println("Starting trading.........")

    System.exit(0)
  }

  def processPrice(price: Price): Unit = {
    (price.asks.headOption.map(_.price.value) |@| price.bids.headOption.map(_.price.value)) (_ - _) match {
      case Some(spread) => currentSpread = (spread * 100000).toInt
      case None =>
    }
  }

  private def processCandle(candle: CandleStick): Unit = {
    println(s"Candle received at ${candle.time}, close price ${candle.close}")
    allCandles = candle +: allCandles

    positionOption.fold {
      if (currentSpread <= maxSpread)
        considerOpeningPosition(allCandles) match {
          case (Some(OpenLongPosition)) => println("Opening long position"); openLongPosition(tradingUnits)
          case (Some(OpenShortPosition)) => println("Opening short position"); openShortPosition(tradingUnits)
          case _ =>
        }
    }(position => if (considerClosingPosition(lastCandles = allCandles, long = isLongPosition(position))) {
      println("Closing position")
      closePositions()
    })
  }

  def considerOpeningPosition(lastCandles: Seq[CandleStick]): Option[OpenPosition] = for {
    candle <- lastCandles.headOption
    previousCandle <- lastCandles.tail.headOption
    macd <- candle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    macdValue <- macd.macd
    macdSignal <- macd.signalLine
    macdHistogram <- macd.histogram
    prevMacd <- previousCandle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    prevMacdValue <- prevMacd.macd
    prevMacdSignal <- prevMacd.signalLine
    prevMacdHistogram <- prevMacd.histogram
    stochastic <- candle.indicator[StochasticCandleIndicator, BigDecimal](stochasticSettings)
    prevStochastic <- previousCandle.indicator[StochasticCandleIndicator, BigDecimal](stochasticSettings)
  } yield {
    if (prevStochastic > 15 && stochastic < 15) OpenLongPosition
    else if (prevStochastic < 85 && stochastic > 85) OpenShortPosition
    else DoNothing
  }

  def considerClosingPosition(lastCandles: Seq[CandleStick], gainLoss: Int = 0, long: Boolean): Boolean = (for {
    candle <- lastCandles.headOption
    previousCandle <- lastCandles.tail.headOption
    macd <- candle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    macdValue <- macd.macd
    macdHistogram <- macd.histogram
    prevMacd <- previousCandle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    prevMacdValue <- prevMacd.macd
    prevMacdHistogram <- prevMacd.histogram
    stochastic <- candle.indicator[StochasticCandleIndicator, BigDecimal](stochasticSettings)
    prevStochastic <- previousCandle.indicator[StochasticCandleIndicator, BigDecimal](stochasticSettings)
    atr <- candle.indicator[ATRCandleIndicator, BigDecimal]("14").map(_.rnd)
  } yield {
    if (
      (long && (prevStochastic < 85 && stochastic > 85)) ||
      (!long && (prevStochastic > 15 && stochastic < 15))
    //      (stochastic > 90 && prevStochastic < 90) || (stochastic < 10 && prevStochastic > 10)
    ) true
    else false
  }).getOrElse(false)

  def updatePosition() = positionOption = Api.positionApi.openPositions(AccountID(chart.accountId)).toOption.flatMap(_.positions.headOption)

  def openLongPosition(units: Int): Unit = {
    Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
      instrument = InstrumentName(chart.instrument),
      units = units,
      timeInForce = TimeInForce.FOK
    ))).fold(error => println(error), order => updatePosition())
  }

  def openShortPosition(units: Int): Unit = {
    Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
      instrument = InstrumentName(chart.instrument),
      units = -units,
      timeInForce = TimeInForce.FOK
    ))).fold(error => println(error), order => updatePosition())
  }

  def closePositions(): Unit = positionOption match {
    case Some(position) =>
      val closePositionRequest = if (isLongPosition(position)) ClosePositionRequest(longUnits = Some("ALL")) else ClosePositionRequest(shortUnits = Some("ALL"))
      Api.positionApi.closePosition(AccountID(chart.accountId), InstrumentName(chart.instrument), closePositionRequest)
        .fold(error => println(error), _ => updatePosition())
    case _ => println("No open positions to close")
  }

  def getPrice: BigDecimal = {
    val \/-(pricing) = Api.pricingApi.pricing(AccountID(chart.accountId), Seq(InstrumentName(chart.instrument)))
    pricing.prices.head.asks.head.price.value
  }

  implicit class BigDecimalPimp(value: BigDecimal) {
    def rnd: BigDecimal = value.setScale(5, HALF_DOWN)
    def toPips: Int = (value * 100000).toInt
  }

  private def isLongPosition(position: Position) = position.long.tradeIDs.nonEmpty

}
