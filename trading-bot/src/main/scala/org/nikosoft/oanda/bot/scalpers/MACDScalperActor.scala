package org.nikosoft.oanda.bot.scalpers

import akka.actor.Actor
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
  case class TrainingStatistics(profitPips: Int = 0, lossPips: Int = 0, openedLong: Option[CandleStick] = None, openedShort: Option[CandleStick] = None)
}

class MACDScalperActor(chart: Chart) extends Actor {

  import MACDScalperActor._

  var allCandles: Seq[CandleStick] = Seq.empty
  var positionOption: Option[Position] = None
  val tradingUnits = 100
  var currentSpread: Int = _
  val maxSpread = 15

  override def preStart(): Unit = {
    updatePosition()
    positionOption.foreach(println)
  }

  def receive: Receive = {
    case candles: Seq[CandleStick] =>
      val (_, stats) = candles.reverse.foldLeft((Seq.empty[CandleStick], TrainingStatistics())) {
        case ((historicalCandles, _stats @ TrainingStatistics(_, _, None, None)), candle) =>
          val toppedUpHistoricalCandles = candle +: historicalCandles
          considerOpeningPosition(toppedUpHistoricalCandles) match {
            case Some(OpenLongPosition) => (toppedUpHistoricalCandles, _stats.copy(openedLong = Some(candle)))
            case Some(OpenShortPosition) => (toppedUpHistoricalCandles, _stats.copy(openedShort = Some(candle)))
            case _ => (toppedUpHistoricalCandles, _stats)
          }

        case ((historicalCandles, _stats @ TrainingStatistics(_, _, longPosition, shortPosition)), candle) =>
          val toppedUpHistoricalCandles = candle +: historicalCandles
          val gainLoss = (longPosition, shortPosition) match {
            case (Some(_openedAt), None) => candle.close - _openedAt.close
            case (None, Some(_openedAt)) => _openedAt.close - candle.close
          }

          if (considerClosingPosition(toppedUpHistoricalCandles, gainLoss)) {
            val (trade, openedAt) = (longPosition, shortPosition) match {
              case (Some(_openedAt), None) => println(s"long position at ${_openedAt.time}, closed at ${candle.time}, ${candle.close - _openedAt.close}"); (((candle.close - _openedAt.close) * 100000).toInt, _openedAt)
              case (None, Some(_openedAt)) => println(s"short position at ${_openedAt.time}, closed at ${candle.time}, ${candle.close - _openedAt.close}"); (((_openedAt.close - candle.close) * 100000).toInt, _openedAt)
            }

            val profitPips = (trade > 0) ? (_stats.profitPips + trade) | _stats.profitPips
            val lossPips = (trade < 0) ? (_stats.lossPips + (-trade)) | _stats.lossPips
            (toppedUpHistoricalCandles, _stats.copy(profitPips = profitPips, lossPips, None, None))
          } else (toppedUpHistoricalCandles, _stats)
      }

      println(s"Profit ${stats.profitPips}, loss ${stats.lossPips}, diff ${stats.profitPips - stats.lossPips}")
      println("Starting trading.........")

    case candle: CandleStick =>
      println(s"Candle received at ${candle.time}, close price ${candle.close}")
      allCandles = candle +: allCandles

      positionOption.fold {
        if (currentSpread <= maxSpread)
          considerOpeningPosition(allCandles) match {
            case (Some(OpenLongPosition)) => println("Opening long position"); openLongPosition(tradingUnits)
            case (Some(OpenShortPosition)) => println("Opening short position"); openShortPosition(tradingUnits)
            case _ =>
          }
      } (_ => if (considerClosingPosition(allCandles)) {
        println("Closing position")
        closePositions()
      })

    case price: Price =>
      (price.asks.headOption.map(_.price.value) |@| price.bids.headOption.map(_.price.value)) (_ - _) match {
        case Some(spread) => currentSpread = (spread * 100000).toInt
        case None =>
      }

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
    stochastic <- candle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
    prevStochastic <- previousCandle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
  } yield {
    if (stochastic > 10 && prevStochastic < 10) OpenLongPosition
    else if (stochastic < 90 && prevStochastic > 90) OpenShortPosition
    else DoNothing
  }

  def considerClosingPosition(lastCandles: Seq[CandleStick], gainLoss: BigDecimal = 0): Boolean = (for {
    candle <- lastCandles.headOption
    previousCandle <- lastCandles.tail.headOption
    macd <- candle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    macdHistogram <- macd.histogram
    prevMacd <- previousCandle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    prevMacdHistogram <- prevMacd.histogram
    stochastic <- candle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
    prevStochastic <- previousCandle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
    atr <- candle.indicator[ATRCandleIndicator, BigDecimal]("14").map(_.rnd)
  } yield {
    if (
      (stochastic > 90 && prevStochastic < 90) || (stochastic < 10 && prevStochastic > 10) || (gainLoss < -0.0023)
    ) true
    else false
  }).getOrElse(false)

  def updatePosition() = positionOption = Api.positionApi.openPositions(AccountID(chart.accountId)).toOption.flatMap(_.positions.headOption)

  def openLongPosition(units: Int): Unit = {
    Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
      instrument = InstrumentName(chart.instrument),
      units = units,
      timeInForce = TimeInForce.FOK
    ))).fold(error => println(error),order => updatePosition())
  }

  def openShortPosition(units: Int): Unit = {
    Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
      instrument = InstrumentName(chart.instrument),
      units = -units,
      timeInForce = TimeInForce.FOK
    ))).fold(error => println(error),order => updatePosition())
  }

  def closePositions(): Unit = {
    Api.positionApi.closePosition(AccountID(chart.accountId), InstrumentName(chart.instrument), ClosePositionRequest(longUnits = Some("ALL"), shortUnits = Some("ALL")))
    updatePosition()
  }

  def getPrice: BigDecimal = {
    val \/-(pricing) = Api.pricingApi.pricing(AccountID(chart.accountId), Seq(InstrumentName(chart.instrument)))
    pricing.prices.head.asks.head.price.value
  }

  implicit class BigDecimalPimp(value: BigDecimal) {
    def rnd: BigDecimal = value.setScale(5, HALF_DOWN)
  }

}
