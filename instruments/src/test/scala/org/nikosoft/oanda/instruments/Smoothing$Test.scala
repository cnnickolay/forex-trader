package org.nikosoft.oanda.instruments

import org.scalatest.{FunSpec, Matchers}

class Smoothing$Test extends FunSpec with Matchers {

  val tolerance = 0.00001

  describe("sma") {
    it("should return None if not enough values for a given range") {
      Smoothing.sma(2, Seq(1)) shouldBe None
    }

    it("should calculate simple moving average") {
      val range = 7
      val input: Seq[BigDecimal] = Seq(1, 2, 3, 4, 5, 77, 32)
      val actual = Smoothing.sma(range, input)
      val expected: BigDecimal = 17.714286
      actual.fold(fail("Should not be None")) { actualValue =>
        actualValue shouldBe (expected +- tolerance)
      }
    }

    it("should calculate simple moving average for any range") {
      val range = 5
      val input: Seq[BigDecimal] = Seq(1, 2, 3, 4, 5, 77, 32)
      val actual = Smoothing.sma(range, input)
      val expected: BigDecimal = 3
      actual.fold(fail("Should not be None")) { actualValue =>
        actualValue shouldBe expected
      }
    }

    it("should return head value for range equal 1") {
      val range = 1
      val input: Seq[BigDecimal] = Seq(1, 2, 3, 4, 5, 77, 32)
      val actual = Smoothing.sma(range, input)
      val expected: BigDecimal = 1
      actual.fold(fail("Should not be None")) { actualValue =>
        actualValue shouldBe expected
      }
    }

  }

  describe("ema") {
    it("should calculate ema") {
      val range = 10
      val value = 22.17
      val lastEma = 23.08
      val expectedValue: BigDecimal = 22.9145454
      val actual = Smoothing.ema(range, value, Option(lastEma))
      actual.fold(fail("Should not be None")) { actualValue =>
        actualValue shouldBe (expectedValue +- tolerance)
      }
    }

    it("should return SMA if last ema is None") {
      val value = 10
      val values: Seq[BigDecimal] = Seq(1, 2, 3, 4, 5)
      val expectedValue: BigDecimal = 3
      val actual = Smoothing.ema(5, value, None, values)
      actual.fold(fail("Should not be None")) { actualValue =>
        actualValue shouldBe expectedValue
      }
    }

    it("should return None if last ema is None and SMA returns None") {
      val value = 10
      val values: Seq[BigDecimal] = Seq(1, 2, 3, 4, 5)
      val tooBigPeriod = 6
      val actual = Smoothing.ema(tooBigPeriod, value, None, values)
      actual shouldBe None
    }

    it("should return None if last ema is None and SMA returns None because there are no values") {
      val value = 10
      val actual = Smoothing.ema(10, value, None, Seq.empty)
      actual shouldBe None
    }
  }

}
