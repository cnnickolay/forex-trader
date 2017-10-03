package org.nikosoft.oanda.instruments

import java.nio.file.Paths
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.{IOResult, scaladsl}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Source}
import akka.util.ByteString
import org.joda.time.{DateTimeField, DateTimeFieldType, Instant, LocalDateTime}
import org.joda.time.format.{DateTimeFormat, DateTimeParser}
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.{Candlestick, CandlestickData}
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity.CandlestickGranularity
import org.nikosoft.oanda.instruments.Oscillators.{MACDItem, StochasticItem}

import scala.concurrent.Future
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

  object CandleStick {
    def toCandleStick(candle: Candlestick, c: CandlestickData): CandleStick = CandleStick(candle.time.toInstant, c.o.value, c.h.value, c.l.value, c.c.value, candle.volume, candle.complete)
  }

  implicit class ChartPimp(chart: Chart) {
    val formatter = DateTimeFormat.forPattern("yyyyMMdd HHmmss")

    def streamCsv(filepath: String, delimiter: String): Source[CandleStick, Future[IOResult]] = {
      val flow: Flow[ByteString, CandleStick, NotUsed] = Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 512, allowTruncation = true)
        .map(_.utf8String)
        .map(_.split(delimiter).toSeq.take(5))
        .map { case date +: open +: high +: low +: close +: _ =>
          val instant = LocalDateTime.parse(date, formatter).toDateTime.toInstant
          CandleStick(instant, BigDecimal(open), BigDecimal(high), BigDecimal(low), BigDecimal(close), 0L, complete = true)
        }
        .mapConcat(chart.addCandleStick(_).toList)

      val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(filepath))
      source.via(flow)
    }
  }

  class Chart(private var candles: Seq[CandleStick] = Seq.empty,
              val indicators: Seq[Indicator[Seq[CandleStick], _]] = Seq.empty) {

    def addCandleStick(candle: CandleStick): Option[CandleStick] = {
      def add(candle: CandleStick): Option[CandleStick] = candle.complete.option {
        val indicatorValues: Map[String, Any] = (indicators, indicators.map(_(candle +: candles))).zipped.collect { case (indicator, Some(value)) => (indicator.toString(), value)}.toMap
        val enrichedCandle = candle.copy(indicators = indicatorValues)
        candles = enrichedCandle +: candles
        enrichedCandle
      }
//      (candle.time.get(DateTimeFieldType.hourOfDay) == 0 &&
//        candle.time.get(DateTimeFieldType.minuteOfHour()) == 0 &&
//        candle.time.get(DateTimeFieldType.secondOfMinute()) == 0
//      ).option(println(candle.time))

      candles match {
        case Nil => add(candle)
        case lastCandle +: _ if lastCandle != candle && lastCandle.time.isBefore(candle.time) => add(candle)
        case _ => None
      }
    }

    def _candles = candles
  }

}
