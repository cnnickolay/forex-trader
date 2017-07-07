package org.nikosoft.oanda.instruments

import java.time.Instant

import org.nikosoft.oanda.instruments.Model._
import org.scalatest.{FunSpec, Matchers}

class Model$Test extends FunSpec with Matchers {

  def checkNumbersMatch(tolerance: Double = 0.01): (BigDecimal, BigDecimal) => Unit = (actual, expected) => actual shouldBe expected +- tolerance

  describe("SMA") {
    it("should calculate simple moving average") {
      val indicator = new SMACandleCloseIndicator(10)
      val chart = new Chart(indicators = Seq(indicator))

      val candleValues: Seq[BigDecimal] = Seq(22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83)
      val expected: Seq[BigDecimal] = Seq(22.22, 22.21, 22.23, 22.26, 22.31, 22.42, 22.61, 22.77, 22.91)
      candleValues
        .map(CandleStick(Instant.now(), 0, 0, 0, _, 0, complete = true))
        .reverse
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
      val chart = new Chart(indicators = Seq(indicator))
      prices
        .map(CandleStick(Instant.now(), 0, 0, 0, _, 0, complete = true))
        .reverse
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
      val chart = new Chart(indicators = Seq(indicator))
      prices
        .map(CandleStick(Instant.now(), 0, 0, 0, _, 0, complete = true))
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
      val chart = new Chart(indicators = Seq(indicator))
      prices
        .map(CandleStick(Instant.now(), 0, 0, 0, _, 0, complete = true))
        .foreach(chart.addCandleStick)

      val actual = indicator._values
      actual should have size expected.size
      (actual, expected).zipped.foreach(checkNumbersMatch(0.1))

    }
  }

}
