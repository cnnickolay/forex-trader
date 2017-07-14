package org.nikosoft.oanda.instruments

import org.apache.commons.lang3.RandomUtils
import org.joda.time.Instant
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity.M5
import org.nikosoft.oanda.instruments.Model._
import org.scalatest.{FunSpec, Matchers}

import scala.language.implicitConversions
import scala.util.Random

class Model$Test extends FunSpec with Matchers {

  def checkNumbersMatch(tolerance: Double = 0.01): (BigDecimal, BigDecimal) => Unit = (actual, expected) => actual shouldBe expected +- tolerance

  describe("SMA") {
    it("should calculate simple moving average") {
      val indicator = new SMACandleCloseIndicator(10)
      val chart = aChart(indicators = Seq(indicator))

      val candleValues: Seq[BigDecimal] = Seq(22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83)
      val expected: Seq[BigDecimal] = Seq(22.22, 22.21, 22.23, 22.26, 22.31, 22.42, 22.61, 22.77, 22.91)
      candleValues
        .reverse
        .zipWithIndex
        .map { case (price, idx) => CandleStick(Instant.now().plus(idx * 1000), 0, 0, 0, price, 0, complete = true) }
        .foreach(chart.addCandleStick)

      val actualValues = indicator._values
      actualValues should have size expected.size
      (actualValues, expected).zipped.foreach(checkNumbersMatch())
    }
  }

  describe("MACD") {
    it("should calculate and accumulate MACD") {
      val prices: Seq[BigDecimal] = Seq(429.8, 434.33, 435.69, 426.98, 426.21, 423.2, 427.72, 431.99, 429.79, 428.91, 442.66, 452.08, 461.14, 463.58, 461.91, 452.73, 452.08, 454.49, 455.72, 443.66, 432.5, 428.35, 428.43, 437.87, 431.72, 430.58, 425.66, 431.14, 420.05, 430.47, 441.4, 444.57, 448.97, 442.8, 450.81, 446.06, 448.85, 459.99)
      val expectedEma12: Seq[BigDecimal] = Seq(433.858256124045, 434.596120873871, 434.644506487302, 434.454416757721, 435.813401622761, 437.559474645081, 440.170288216914, 442.433976983626, 444.33288188974, 446.977042233329, 450.261959003025, 451.644133367212, 451.564884888523, 449.823954868254, 447.322855753392, 444.670647708554, 443.205310928291, 441.591731097071, 439.246591296538, 436.251426077727, 434.904412637314, 435.341578571371, 436.612774675257, 438.10055188894, 438.142470414201, 439.310192307692, 440.8975)
      val expectedEma26: Seq[BigDecimal] = Seq(436.260882396911, 436.777752988664, 436.973573227757, 437.076259085978, 437.883959812856, 438.817876597885, 440.067306725715, 441.055091263773, 441.780298564874, 442.739522450064, 443.845884246069, 443.940754985755, 443.289615384615)
      val expectedMacd: Seq[BigDecimal] = Seq(-2.40262627286643, -2.18163211479299, -2.32906674045495, -2.62184232825689, -2.07055819009491, -1.25840195280313, 0.102981491199102, 1.37888571985366, 2.55258332486574, 4.23751978326482, 6.41607475695588, 7.70337838145673, 8.27526950390762)
      val expectedSignalLine: Seq[BigDecimal] = Seq(-0.152012994298479, 0.410640325343509, 1.05870843537763, 1.90565222933578, 3.03752586873394)
      val expectedHistogram: Seq[BigDecimal] = Seq(-2.25061327856795, -2.5922724401365, -3.38777517583258, -4.52749455759267, -5.10808405882886)

      val indicator = new MACDCandleCloseIndicator()
      val chart = aChart(indicators = Seq(indicator))
      prices
        .reverse
        .zipWithIndex
        .map { case (price, idx) => CandleStick(Instant.now().plus(idx * 1000), 0, 0, 0, price, 0, complete = true) }
        .foreach(chart.addCandleStick)

      val actualEma12 = indicator._values.flatMap(_.ema12)
      actualEma12 should have size expectedEma12.size
      (actualEma12, expectedEma12).zipped.foreach(checkNumbersMatch())

      val actualEma26 = indicator._values.flatMap(_.ema26)
      actualEma26 should have size expectedEma26.size
      (actualEma26, expectedEma26).zipped.foreach(checkNumbersMatch())

      val actualMacd = indicator._values.flatMap(_.macd)
      actualMacd should have size expectedMacd.size
      (actualMacd, expectedMacd).zipped.foreach(checkNumbersMatch())

      val actualSignalLine = indicator._values.flatMap(_.signalLine)
      actualSignalLine should have size expectedSignalLine.size
      (actualSignalLine, expectedSignalLine).zipped.foreach(checkNumbersMatch())

      val actualHistogram = indicator._values.flatMap(_.histogram)
      actualHistogram should have size expectedHistogram.size
      (actualHistogram, expectedHistogram).zipped.foreach(checkNumbersMatch())
    }
  }

  describe("EMA") {
    it("should calculate exponential moving average") {
      val prices: Seq[BigDecimal] = Seq(22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83)
      val expectedEma: Seq[BigDecimal] = Seq(23.13, 22.97, 22.80, 22.52, 22.33, 22.27, 22.24, 22.21, 22.22)

      val indicator = new EMACandleCloseIndicator(10)
      val chart = aChart(indicators = Seq(indicator))
      prices
        .zipWithIndex
        .map { case (price, idx) => CandleStick(Instant.now().plus(idx * 1000), 0, 0, 0, price, 0, complete = true) }
        .foreach(chart.addCandleStick)

      val actualEma = indicator._values
      actualEma should have size expectedEma.size
      (actualEma, expectedEma).zipped.foreach(checkNumbersMatch())
    }
  }

  describe("RSI") {
    it("should calculate Relative Strength Index") {
      val prices: Seq[BigDecimal] = Seq(44.34, 44.09, 44.15, 43.61, 44.33, 44.83, 45.10, 45.42, 45.84, 46.08, 45.89, 46.03, 45.61, 46.28, 46.28, 46.00, 46.03, 46.41, 46.22, 45.64, 46.21, 46.25)
      val expected: Seq[BigDecimal] = Seq(63.26, 62.93, 57.97, 66.36, 69.41, 66.55, 66.32, 70.53)

      val indicator = new RSICandleCloseIndicator(14)
      val chart = aChart(indicators = Seq(indicator))
      prices
        .zipWithIndex
        .map { case (price, idx) => CandleStick(Instant.now().plus(idx * 1000), 0, 0, 0, price, 0, complete = true) }
        .foreach(chart.addCandleStick)

      val actual = indicator._values
      actual should have size expected.size
      (actual, expected).zipped.foreach(checkNumbersMatch(0.1))

    }
  }

  describe("Chart") {
    it("should ignore candle if it's already in the list of candles") {
      val chart = aChart()
      val candle = aCandleStick.copy(complete = true)

      chart._candles should have size 0
      chart.addCandleStick(candle) shouldBe Some(candle)
      chart._candles should have size 1
      chart.addCandleStick(candle) shouldBe None
      chart._candles should have size 1
    }

    it("should not add not complete candle") {
      val chart = aChart()
      val candle = aCandleStick.copy(complete = false)

      chart._candles should have size 0
      chart.addCandleStick(candle) shouldBe None
      chart._candles should have size 0
    }

    it("only candles with future dates can be added, i.e. no past candles can be added") {
      val chart = aChart()
      val candle = aCandleStick.copy(complete = true)
      val pastCandle = candle.copy(time = candle.time.minus(1))

      chart._candles should have size 0
      chart.addCandleStick(candle) shouldBe Some(candle)
      chart._candles should have size 1
      chart.addCandleStick(pastCandle) shouldBe None
      chart._candles should have size 1
    }
  }

  describe("ATR") {

    it("should calculate Average True Range and accumulate values") {
      val indicator = new ATRCandleIndicator(14)
      val chart = aChart(indicators = Seq(indicator))

      val inputHigh: Seq[BigDecimal] = Seq(48.70, 48.72, 48.90, 48.87, 48.82, 49.05, 49.20, 49.35, 49.92, 50.19, 50.12, 49.66, 49.88, 50.19, 50.36, 50.57, 50.65)
      val inputLow: Seq[BigDecimal] = Seq(47.79, 48.14, 48.39, 48.37, 48.24, 48.64, 48.94, 48.86, 49.50, 49.87, 49.20, 48.90, 49.43, 49.73, 49.26, 50.09, 50.30)
      val inputClose: Seq[BigDecimal] = Seq(48.16, 48.61, 48.75, 48.63, 48.74, 49.03, 49.07, 49.32, 49.91, 50.13, 49.53, 49.50, 49.75, 50.03, 50.31, 50.52, 50.41)

      (inputHigh, inputLow, inputClose).zipped.map(CandleStick(Instant.now, 0, _, _, _, 0, complete = true))
        .zipWithIndex
        .map { case (candle, idx) => candle.copy(time = Instant.now().plus(idx * 1000)) }
        .foreach(chart.addCandleStick)

      val expected: Seq[BigDecimal] = Seq(0.57, 0.59, 0.59, 0.56)

      val actualValues = indicator._values
      actualValues should have size expected.size
      (actualValues, expected).zipped.foreach(checkNumbersMatch())
    }
  }

  describe("CMO") {
    it("should calculate Chande Momentum Oscillator") {
      val indicator = new CMOCandleCloseIndicator(21)
      val chart = aChart(indicators = Seq(indicator))

      val prices = Seq[BigDecimal](20515, 20385, 19775, 19525, 19125, 19305, 19245, 18635, 18505, 19005, 19025, 18850, 19065, 19050, 19475, 18915, 18980, 18605, 18310, 17980, 18235, 17845, 18315, 18540, 19105, 19225, 19355, 19560)
      val expected = Seq[BigDecimal](-40.7143, -43.3447, -25.5245, -17.2959, -0.3413, -1.3793, 1.8739, 16.9259).reverse
      prices
        .zipWithIndex
        .map { case (price, idx) => CandleStick(Instant.now().plus(idx * 1000), 0, 0, 0, price, 0, complete = true) }
        .foreach(chart.addCandleStick)

      val actual = indicator._values
      actual should have size expected.size
      (actual, expected).zipped.foreach(checkNumbersMatch(1e-4))
    }
  }

  describe("Stochastic") {
    implicit def highLowToCandle(highLow: (Double, Double)): CandleStick = CandleStick(Instant.now, 0, highLow._1, highLow._2, 0, 0, complete = true)
    implicit def highLowCloseToCandle(highLowClose: (Double, Double, Double)): CandleStick = CandleStick(Instant.now, 0, highLowClose._1, highLowClose._2, highLowClose._3, 0, complete = true)

    val prices = Seq[CandleStick](
      (127.009, 125.3574),
      (127.6159, 126.1633),
      (126.5911, 124.9296),
      (127.3472, 126.0937),
      (128.173, 126.8199),
      (128.4317, 126.4817),
      (127.3671, 126.034),
      (126.422, 124.8301),
      (126.8995, 126.3921),
      (126.8498, 125.7156),
      (125.646, 124.5615),
      (125.7156, 124.5715),
      (127.1582, 125.0689),
      (127.7154, 126.8597, 127.2876),
      (127.6855, 126.6309, 127.1781),
      (128.2228, 126.8001, 128.0138),
      (128.2725, 126.7105, 127.1085),
      (128.0934, 126.8001, 127.7253),
      (128.2725, 126.1335, 127.0587),
      (127.7353, 125.9245, 127.3273)
    )

    it("should calculate Stochastic oscillator only for fast value") {
      val indicator = new StochasticCandleIndicator(14, None, None)
      val chart = aChart(indicators = Seq(indicator))

      val expected = Seq[BigDecimal](74.52, 64.52, 81.74, 65.81, 89.20, 67.60, 70.43)

      prices
        .zipWithIndex
        .map { case (price, idx) => price.copy(time = Instant.now().plus(idx * 1000)) }
        .foreach(chart.addCandleStick)

      val actual = indicator._values
      actual should have size expected.size
      (actual, expected).zipped.foreach(checkNumbersMatch(1e-2))
    }

    it("should calculate Stochastic oscillator only for smoothed value") {
      val indicator = new StochasticCandleIndicator(14, Some(3), None)
      val chart = aChart(indicators = Seq(indicator))

      val expected = Seq[BigDecimal](73.60, 70.69, 78.92, 74.20, 75.74)

      prices
        .zipWithIndex
        .map { case (price, idx) => price.copy(time = Instant.now().plus(idx * 1000)) }
        .foreach(chart.addCandleStick)

      val actual = indicator._values
      actual should have size expected.size
      (actual, expected).zipped.foreach(checkNumbersMatch(1e-2))
    }

    it("should calculate Stochastic oscillator only for smoothed again value :-D") {
      val indicator = new StochasticCandleIndicator(14, Some(3), Some(3))
      val chart = aChart(indicators = Seq(indicator))

      val expected = Seq[BigDecimal](74.40, 74.60, 76.29)

      prices
        .zipWithIndex
        .map { case (price, idx) => price.copy(time = Instant.now().plus(idx * 1000)) }
        .foreach(chart.addCandleStick)

      val actual = indicator._values
      actual should have size expected.size
      (actual, expected).zipped.foreach(checkNumbersMatch(1e-2))
    }
  }

  def aCandleStick = CandleStick(Instant.now(), Random.nextInt(100), Random.nextInt(100), Random.nextInt(100), Random.nextInt(100), RandomUtils.nextLong(), complete = false)

  def aChart[A](indicators: Seq[Indicator[Seq[CandleStick], A]] = Seq.empty) = new Chart(accountId = "123", instrument = "EUR_USD", granularity = M5, indicators = indicators)
}
