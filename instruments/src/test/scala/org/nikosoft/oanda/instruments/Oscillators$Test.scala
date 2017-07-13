package org.nikosoft.oanda.instruments

import org.scalatest.{FunSpec, Matchers}
import org.nikosoft.oanda.instruments.Oscillators.{MACDItem, macd, rsi}
import scalaz.Scalaz._

class Oscillators$Test extends FunSpec with Matchers {

  describe("CMO") {
    it("should calculate cmo at the beginning") {
      val input: Seq[BigDecimal] = Seq(18235, 17980, 18310, 18605, 18980, 18915, 19475, 19050, 19065, 18850, 19025, 19005, 18505, 18635, 19245, 19305, 19125, 19525, 19775, 20385, 20515)
      val expected: BigDecimal = -40.7143
      val actual = CMO.cmo(21, input)
      actual.fold(fail("this should return something")) { _ shouldBe (expected +- 1e-4) }
    }

    it("should calculate cmo when there is more values than required by the period") {
      val input: Seq[BigDecimal] = Seq(17845, 18235, 17980, 18310, 18605, 18980, 18915, 19475, 19050, 19065, 18850, 19025, 19005, 18505, 18635, 19245, 19305, 19125, 19525, 19775, 20385, 20515)
      val expected: BigDecimal = -43.3447
      val actual = CMO.cmo(21, input)
      actual.fold(fail("this should return something")) { _ shouldBe (expected +- 1e-4) }
    }

    it("should return None if period is greater than amount of values") {
      val input: Seq[BigDecimal] = Seq(1, 2)
      CMO.cmo(3, input) shouldBe None
      CMO.cmo(2, input) shouldNot be(None)
    }
  }
  
  describe("rsi") {
    it("should calculate rsi for array of values") {
      val input: Seq[BigDecimal] = Seq(46.28, 46.28, 45.61, 46.03, 45.89, 46.08, 45.84, 45.42, 45.10, 44.83, 44.33, 43.61, 44.15, 44.09, 44.34)
      val expected: BigDecimal = 70.46
      val actual = rsi(14, None, input)
      actual.fold(fail("Should not be None")) { case (actualValue, _, _) =>
        actualValue shouldBe (expected +- 1e-2)
      }
    }
  }

  describe("macd") {
    implicit def toOptional(value: Double): Option[BigDecimal] = BigDecimal.valueOf(value).some
    val tolerance = 0.0000001

    it("should calculate MACD when all values are given") {
      val actual = macd(426.98, MACDItem(426.98, 435.813401622761, 437.883959812856, -2.07055819009491, 3.03752586873394) :: Nil)
      val expectedEma12: BigDecimal = 434.454416757721
      val expectedEma26: BigDecimal = 437.076259085978
      val expectedMacd: BigDecimal = -2.62184232825689
      val expectedSignalLine: BigDecimal = 1.90565222933578
      val expectedHistogramValue: BigDecimal = -4.52749455759

      import actual._
      ~ema12 shouldBe (expectedEma12 +- tolerance)
      ~ema26 shouldBe (expectedEma26 +- tolerance)
      ~signalLine shouldBe (expectedSignalLine +- tolerance)
      ~actual.macd shouldBe (expectedMacd +- tolerance)
      ~histogram shouldBe (expectedHistogramValue +- tolerance)
    }

    it("should not calculate MACD when no values are set") {
      val actual = macd(426.98, Seq.empty)

      import actual._
      price shouldBe 426.98
      ema12 shouldBe None
      ema26 shouldBe None
      signalLine shouldBe None
      actual.macd shouldBe None
      histogram shouldBe None
    }

    it("should return MACDItem with only 12 day EMA set when previous MACDItem is None and only 12 values given") {
      val values: Seq[BigDecimal] = Seq(459.99, 448.85, 446.06, 450.81, 442.8, 448.97, 444.57, 441.4, 430.47, 420.05, 431.14)
      val actual = macd(425.66, values.map(MACDItem(_)))
      val expectedEma12: BigDecimal = 440.8975

      import actual._
      ~ema12 shouldBe (expectedEma12 +- tolerance)
      ema26 shouldBe None
      signalLine shouldBe None
      actual.macd shouldBe None
      histogram shouldBe None
    }

  }

}
