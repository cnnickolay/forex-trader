package org.nikosoft.oanda.instruments

import java.time.Instant
import scalaz.Scalaz._

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
    def apply(input: (Int, Option[(BigDecimal, BigDecimal)], Seq[BigDecimal])): Option[(BigDecimal, BigDecimal, BigDecimal)] = (Oscillators._rsi _).tupled(input)
  }
  case object MACDIndicator extends IndicatorType[(BigDecimal, Seq[MACDItem]), MACDItem] {
    def apply(input: (BigDecimal, Seq[MACDItem])): Option[MACDItem] = Some((Oscillators.macd _).tupled(input))
  }

  abstract class Indicator[INPUT, OUTPUT] extends (INPUT => Seq[OUTPUT]) {
    val indicatorType: IndicatorType[_, OUTPUT]
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
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles => indicatorType((period, candles.map(_.close)))
    val indicatorType = SMAIndicator
  }
  class EMACandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: (Seq[CandleStick]) => Option[BigDecimal] =
      candles => candles.headOption.flatMap(candle => indicatorType((period, candle.close, _values.headOption, candles.take(period).map(_.close))))

    val indicatorType = EMAIndicator
  }
  class RSICandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles =>
      indicatorType((period, gainLoss, candles.map(_.close))).map { case (rsi, gain, loss) =>
        gainLoss = (gain, loss).some
        rsi
      }
    val indicatorType = RSIIndicator
  }
  class MACDCandleCloseIndicator extends Indicator[Seq[CandleStick], MACDItem] {
    protected def enrichFunction: Seq[CandleStick] => Option[MACDItem] = candles => candles.headOption.flatMap(candle => indicatorType((candle.close, _values)))
    val indicatorType = MACDIndicator
  }

  case class CandleStick(time: Instant,
                         open: BigDecimal,
                         high: BigDecimal,
                         low: BigDecimal,
                         close: BigDecimal,
                         volume: Long,
                         complete: Boolean)

  class Chart(private var candles: Seq[CandleStick] = Seq.empty, val indicators: Seq[Indicator[Seq[CandleStick], _]]) {
    def addCandleStick(candle: CandleStick) = {
      candles = candle +: candles
      indicators.foreach(indicator => indicator(candles))
    }
  }

}
