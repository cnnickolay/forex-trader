package org.nikosoft.oanda.instruments

import org.joda.time.Instant
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity.CandlestickGranularity

import scalaz.Scalaz._
import scalaz._
import Scalaz._
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

object Model {

  sealed trait IndicatorType[T, K] extends (T => Option[K])

  case object EMAIndicator extends IndicatorType[(Int, BigDecimal, Option[BigDecimal], Seq[BigDecimal]), BigDecimal] {
    def apply(input: (Int, BigDecimal, Option[BigDecimal], Seq[BigDecimal])): Option[BigDecimal] = (Smoothing.ema _).tupled(input)
  }
  case object SMAIndicator extends IndicatorType[(Int, Seq[BigDecimal]), BigDecimal] {
    def apply(input: (Int, Seq[BigDecimal])): Option[BigDecimal] = Smoothing.sma(input._1, input._2)
  }
  case object RSIIndicator extends IndicatorType[(Int, Option[(BigDecimal, BigDecimal)], Seq[BigDecimal]), (BigDecimal, BigDecimal, BigDecimal)] {
    def apply(input: (Int, Option[(BigDecimal, BigDecimal)], Seq[BigDecimal])): Option[(BigDecimal, BigDecimal, BigDecimal)] = (Oscillators.rsi _).tupled(input)
  }
  case object MACDIndicator extends IndicatorType[(BigDecimal, Seq[MACDItem]), MACDItem] {
    def apply(input: (BigDecimal, Seq[MACDItem])): Option[MACDItem] = Some((Oscillators.macd _).tupled(input))
  }

  abstract class Indicator[INPUT, OUTPUT] extends (INPUT => Seq[OUTPUT]) {
    protected var values: Seq[OUTPUT] = Seq.empty
    protected def enrichFunction: INPUT => Option[OUTPUT]
    def apply(input: INPUT): Seq[OUTPUT] = {
      enrichFunction(input).fold(values) { enriched =>
        values = enriched +: values
        values
      }
    }
    def _values = values
  }

  class SMACandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles => SMAIndicator((period, candles.map(_.close)))
  }
  class EMACandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: (Seq[CandleStick]) => Option[BigDecimal] =
      candles => candles.headOption.flatMap(candle => EMAIndicator((period, candle.close, _values.headOption, candles.take(period).map(_.close))))
  }
  class RSICandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    var gainLoss: Option[(BigDecimal, BigDecimal)] = None
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles =>
      RSIIndicator((period, gainLoss, candles.map(_.close))).map { case (rsi, gain, loss) =>
        gainLoss = (gain, loss).some
        rsi
      }
  }
  class MACDCandleCloseIndicator extends Indicator[Seq[CandleStick], MACDItem] {
    protected def enrichFunction: Seq[CandleStick] => Option[MACDItem] = candles => candles.headOption.flatMap(candle => MACDIndicator((candle.close, _values)))
  }

  case class CandleStick(time: Instant,
                         open: BigDecimal,
                         high: BigDecimal,
                         low: BigDecimal,
                         close: BigDecimal,
                         volume: Long,
                         complete: Boolean)

  class Chart(val accountId: String,
              val instrument: String,
              val granularity: CandlestickGranularity,
              var candles: Seq[CandleStick] = Seq.empty,
              val indicators: Seq[Indicator[Seq[CandleStick], _]] = Seq.empty) {

    def addCandleStick(candle: CandleStick): Option[CandleStick] = {
      def add(candle: CandleStick): Option[CandleStick] = Option(candle.complete).collect { case true => // looks crappy, I know
        candles = candle +: candles
        indicators.foreach(_(candles))
        candle
      }

      candles match {
        case Nil => add(candle)
        case lastCandle +: _ if lastCandle != candle && lastCandle.time.isBefore(candle.time) => add(candle)
        case _ => None
      }
    }

    def _candles = candles
  }

}
