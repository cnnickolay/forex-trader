package org.nikosoft.oanda.bot.scalpers

import akka.actor.Actor
import org.nikosoft.oanda.api.ApiModel.PricingModel.Price
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, MACDCandleCloseIndicator, StochasticCandleIndicator}
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

object MACDScalperActor {

  trait OpenPosition

  case object OpenShortPosition extends OpenPosition

  case object OpenLongPosition extends OpenPosition

  case object DoNothing extends OpenPosition

  case class TrainingStatistics(profitPips: Int = 0, lossPips: Int = 0)

}

class MACDScalperActor(chart: Chart) extends Actor {

  import MACDScalperActor._

  var allCandles: Seq[CandleStick] = Seq.empty

  def receive: Receive = {
    case candles: Seq[CandleStick] =>
      candles.reverse.foldLeft((Seq.empty[CandleStick], Option.empty[CandleStick], TrainingStatistics())) {
        case ((historicalCandles, None), candle) =>
          val toppedUpHistoricalCandles = candle +: historicalCandles
          considerOpeningPosition(toppedUpHistoricalCandles) match {
            case Some(OpenLongPosition) =>
              val openedAt = Some(candle)
              (toppedUpHistoricalCandles, openedAt)
            case _ => (toppedUpHistoricalCandles, None)
          }
        case ((historicalCandles, openedAtOption @ Some(openedAt)), candle) =>
          val toppedUpHistoricalCandles = candle +: historicalCandles
          if (considerClosingPosition(toppedUpHistoricalCandles)) {
            println(s"opened position at ${openedAt.time}, closed at ${candle.time}, ${candle.close - openedAt.close}")
            (toppedUpHistoricalCandles, None)
          } else (toppedUpHistoricalCandles, openedAtOption)
      }

    case candle: CandleStick =>

    case price: Price =>

  }

  def considerOpeningPosition(lastCandles: Seq[CandleStick]): Option[OpenPosition] = for {
    candle <- lastCandles.headOption
    previousCandle <- lastCandles.tail.headOption
    macd <- candle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    macdHistogram <- macd.histogram
    prevMacd <- previousCandle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    prevMacdHistogram <- prevMacd.histogram
    stochastic <- candle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
  } yield {
    if (prevMacdHistogram < 0 && macdHistogram > 0) OpenLongPosition
    else DoNothing
  }

  def considerClosingPosition(lastCandles: Seq[CandleStick]): Boolean = (for {
    candle <- lastCandles.headOption
    previousCandle <- lastCandles.tail.headOption
    macd <- candle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    macdHistogram <- macd.histogram
    prevMacd <- previousCandle.indicator[MACDCandleCloseIndicator, MACDItem](None)
    prevMacdHistogram <- prevMacd.histogram
  } yield {
    if (macdHistogram < 0 && prevMacdHistogram > 0) true
    else false
  }).getOrElse(false)
}
