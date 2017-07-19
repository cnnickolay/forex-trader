package org.nikosoft.oanda.instruments

import org.joda.time.Instant
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity.CandlestickGranularity
import org.nikosoft.oanda.instruments.Oscillators.{MACDItem, StochasticItem}

import scalaz.Scalaz._

object Model {

  sealed trait IndicatorType[T, K] extends (T => Option[K])

  case object Stochastic extends IndicatorType[(Int, Option[Int], Option[Int], Seq[CandleStick], Seq[StochasticItem]), StochasticItem] {
    def apply(input: (Int, Option[Int], Option[Int], Seq[CandleStick], Seq[StochasticItem])): Option[StochasticItem] = (Oscillators.stochastic _).tupled(input)
  }
  case object CMOIndicator extends IndicatorType[(Int, Seq[BigDecimal]), BigDecimal] {
    def apply(input: (Int, Seq[BigDecimal])): Option[BigDecimal] = (Oscillators.cmo _).tupled(input)
  }
  case object ATRIndicator extends IndicatorType[(Int, Seq[CandleStick], Option[BigDecimal]), BigDecimal] {
    def apply(input: (Int, Seq[CandleStick], Option[BigDecimal])): Option[BigDecimal] = (Indicators.atr _).tupled(input)
  }
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

  abstract class Indicator[INPUT, OUTPUT] extends (INPUT => Option[OUTPUT]) {
    protected var values: Seq[OUTPUT] = Seq.empty
    protected def enrichFunction: INPUT => Option[OUTPUT]
    def apply(input: INPUT): Option[OUTPUT] = {
      enrichFunction(input).fold[Option[OUTPUT]](None) { enriched =>
        values = enriched +: values
        Some(enriched)
      }
    }
    def _values = values

    override def toString(): String = this.getClass.getSimpleName
  }

  class StochasticCandleIndicator(period: Int, smoothingPeriod: Option[Int], secondSmoothingPeriod: Option[Int]) extends Indicator[Seq[CandleStick], BigDecimal] {
    var stochastics = Seq.empty[StochasticItem]
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = { candles =>
      val stochastic = Stochastic((period, smoothingPeriod, secondSmoothingPeriod, candles, stochastics))
      stochastics = stochastic.toSeq ++ stochastics
      stochastic.flatMap(value => {
        (smoothingPeriod, secondSmoothingPeriod) match {
          case (_, Some(_)) => value.smoothedAgain
          case (Some(_), None) => value.smoothed
          case (None, None) => Some(value.fastValue)
        }
      })
    }

    override def toString(): String = super.toString() + "_" + (Seq(period) ++ smoothingPeriod.toSeq ++ secondSmoothingPeriod.toSeq).mkString("_")
  }
  class CMOCandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles => CMOIndicator((period, candles.map(_.close)))
    override def toString(): String = super.toString() + s"_$period"
  }
  class SMACandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles => SMAIndicator((period, candles.map(_.close)))
    override def toString(): String = super.toString() + s"_$period"
  }
  class ATRCandleIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: (Seq[CandleStick]) => Option[BigDecimal] = candles => ATRIndicator(period, candles, _values.headOption)
    override def toString(): String = super.toString() + s"_$period"
  }
  class EMACandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    protected def enrichFunction: (Seq[CandleStick]) => Option[BigDecimal] =
      candles => candles.headOption.flatMap(candle => EMAIndicator((period, candle.close, _values.headOption, candles.take(period).map(_.close))))
    override def toString(): String = super.toString() + s"_$period"
  }
  class RSICandleCloseIndicator(period: Int) extends Indicator[Seq[CandleStick], BigDecimal] {
    var gainLoss: Option[(BigDecimal, BigDecimal)] = None
    protected def enrichFunction: Seq[CandleStick] => Option[BigDecimal] = candles =>
      RSIIndicator((period, gainLoss, candles.map(_.close))).map { case (rsi, gain, loss) =>
        gainLoss = (gain, loss).some
        rsi
      }
    override def toString(): String = super.toString() + s"_$period"
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
                         complete: Boolean,
                         indicators: Map[String, Any] = Map.empty) {
    def indicator[T <: Indicator[_, OUTPUT], OUTPUT](conf: Option[String])(implicit manifest: Manifest[T]): Option[OUTPUT] = indicators.get(manifest.runtimeClass.getSimpleName + s"${conf.fold("")(s => "_" + s)}").map(_.asInstanceOf[OUTPUT])
    def indicator[T <: Indicator[_, OUTPUT], OUTPUT](conf: String)(implicit manifest: Manifest[T]): Option[OUTPUT] = indicator[T, OUTPUT](Some(conf))
  }

  class Chart(val accountId: String,
              val instrument: String,
              val granularity: CandlestickGranularity,
              private var candles: Seq[CandleStick] = Seq.empty,
              val indicators: Seq[Indicator[Seq[CandleStick], _]] = Seq.empty) {

    def addCandleStick(candle: CandleStick): Option[CandleStick] = {
      def add(candle: CandleStick): Option[CandleStick] = candle.complete.option {
        val indicatorValues: Map[String, Any] = (indicators, indicators.map(_(candle +: candles))).zipped.collect { case (indicator, Some(value)) => (indicator.toString(), value)}.toMap
        val enrichedCandle = candle.copy(indicators = indicatorValues)
        candles = enrichedCandle +: candles
        enrichedCandle
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
