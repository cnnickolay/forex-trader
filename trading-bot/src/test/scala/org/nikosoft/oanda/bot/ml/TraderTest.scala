package org.nikosoft.oanda.bot.ml

import org.joda.time.DateTime
import org.nikosoft.oanda.bot.scalping.Model.{LimitOrder, MarketOrder, Order, Position, PositionType, StopLossOrder, TakeProfitOrder, Trade, TradeType}
import org.nikosoft.oanda.bot.scalping.{Trader, TradingModel}
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.scalatest.{FunSpec, FunSuite, Matchers}

class TraderTest extends FunSpec with Matchers {

  describe("market orders") {
    it("Market order") {
      val input = List(
        CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true)
      )

      val trader = new Trader(0, new TradingModel {
        override val commission: Int = 0

        override def createOrder(candle: CandleStick) = Option(MarketOrder(PositionType.ShortPosition, candle, Nil))
      })

      input.foreach(trader.processCandles)

      trader.trades shouldBe Nil
      trader.orders shouldBe Nil
      trader.positionOption shouldBe Some(Position(
        creationOrder = MarketOrder(PositionType.ShortPosition, input.head, Nil),
        executionPrice = 1.10007,
        executionCandle = input(1),
        positionType = PositionType.ShortPosition
      ))
    }

    it("chained orders get unfolded when main order is executed") {
      val input = List(
        CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true)
      )

      val trader = new Trader(0, new TradingModel {
        override val commission: Int = 0

        override def createOrder(candle: CandleStick) = Option(MarketOrder(PositionType.ShortPosition, candle, List(
          TakeProfitOrder(candle, 1.10000, positionType = PositionType.ShortPosition),
          StopLossOrder(candle, 1.20000, positionType = PositionType.ShortPosition)
        )))
      })

      input.foreach(trader.processCandles)

      trader.trades shouldBe Nil
      trader.orders shouldBe List(
        TakeProfitOrder(input.head, 1.10000, positionType = PositionType.ShortPosition),
        StopLossOrder(input.head, 1.20000, positionType = PositionType.ShortPosition)
      )
      trader.positionOption shouldNot be(None)
    }

    describe("stop loss") {
      it("Short market order with stop loss order which closes in the same candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10009, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10015
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.ShortPosition, candle,
            List(StopLossOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.ShortPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(1)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.StopLoss

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }

      it("Short market order with stop loss order which closes in the next candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10009, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10018
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.ShortPosition, candle,
            List(StopLossOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.ShortPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(2)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.StopLoss

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }

      it("Long market order with stop loss order which closes in the same candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10009, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10004
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.LongPosition, candle,
            List(StopLossOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.LongPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(1)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.StopLoss

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }

      it("Long market order with stop loss order which closes in the next candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10007, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10002, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10003
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.LongPosition, candle,
            List(StopLossOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.LongPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(2)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.StopLoss

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }
    }

    describe("take profit") {
      it("Short market order with take profit order which closes in the same candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10009, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10004
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.ShortPosition, candle,
            List(TakeProfitOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.ShortPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(1)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.TakeProfit

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }

      it("Long market order with take profit order which closes in the same candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10009, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.100014
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.LongPosition, candle,
            List(TakeProfitOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.LongPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(1)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.TakeProfit

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }

      it("Short market order with take profit order which closes in the next candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10007, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10002, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10003
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.ShortPosition, candle,
            List(TakeProfitOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.ShortPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(2)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.TakeProfit

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }

      it("Long market order with take profit order which closes in the next candle") {
        val input = List(
          CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10015, 1.10003, 1.10008, 0, complete = true),
          CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10020, 1.10009, 1.10010, 0, complete = true)
        )

        val stopLossPrice = 1.10019
        val trader = new Trader(0, new TradingModel {
          override val commission: Int = 0

          override def createOrder(candle: CandleStick) = if (candle == input.head) Option(MarketOrder(PositionType.LongPosition, candle,
            List(TakeProfitOrder(orderCreatedAt = candle, price = stopLossPrice, positionType = PositionType.LongPosition))
          )) else None
        })

        input.foreach(trader.processCandles)

        val trade = trader.trades.head
        trade.position.creationOrder.orderCreatedAt shouldBe input.head
        trade.position.executionCandle shouldBe input(1)
        trade.position.executionPrice shouldBe 1.10007
        trade.orderClosedAt shouldBe input(2)
        trade.closedAtPrice shouldBe stopLossPrice
        trade.tradeType shouldBe TradeType.TakeProfit

        trader.orders shouldBe Nil
        trader.positionOption shouldBe None
      }
    }

  }

  describe("limit orders") {
    it("sells when price reached") {
      val input = List(
        CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10017, 1.10003, 1.10008, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10019, 1.10005, 1.10010, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:15:00Z").toInstant, 1.10011, 1.10021, 1.10007, 1.10012, 0, complete = true)
      )

      val targetPrice = 1.10020

      val trader = new Trader(0, new TradingModel {
        override val commission: Int = 0

        override def createOrder(candle: CandleStick) = Option(LimitOrder(price = targetPrice, PositionType.ShortPosition, candle, Nil))
      })

      input.foreach(trader.processCandles)

      trader.trades shouldBe Nil
      trader.orders shouldBe Nil
      trader.positionOption shouldBe Some(Position(
        creationOrder = LimitOrder(price = targetPrice, PositionType.ShortPosition, input.head, Nil),
        executionPrice = targetPrice,
        executionCandle = input.last,
        positionType = PositionType.ShortPosition
      ))
    }

    it("does not sell if price is not reached") {
      val input = List(
        CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10005, 1.10010, 1.10000, 1.10003, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10007, 1.10017, 1.10003, 1.10008, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10009, 1.10019, 1.10005, 1.10010, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:15:00Z").toInstant, 1.10011, 1.10021, 1.10007, 1.10012, 0, complete = true)
      )

      val targetPrice = 1.10022

      val trader = new Trader(0, new TradingModel {
        override val commission: Int = 0

        override def createOrder(candle: CandleStick) = Option(LimitOrder(price = targetPrice, PositionType.ShortPosition, candle, Nil))
      })

      input.foreach(trader.processCandles)

      trader.trades shouldBe Nil
      trader.orders shouldBe List(LimitOrder(price = targetPrice, PositionType.ShortPosition, input.head, Nil))
      trader.positionOption shouldBe None
    }

    it("buys when price reached") {
      val input = List(
        CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10011, 1.10021, 1.10007, 1.10012, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10009, 1.10019, 1.10005, 1.10010, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10007, 1.10017, 1.10003, 1.10008, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:15:00Z").toInstant, 1.10005, 1.10010, 1.10001, 1.10003, 0, complete = true)
      )

      val targetPrice = 1.10002

      val trader = new Trader(0, new TradingModel {
        override val commission: Int = 0

        override def createOrder(candle: CandleStick) = Option(LimitOrder(price = targetPrice, PositionType.LongPosition, candle, Nil))
      })

      input.foreach(trader.processCandles)

      trader.trades shouldBe Nil
      trader.orders shouldBe Nil
      trader.positionOption shouldBe Some(Position(
        creationOrder = LimitOrder(price = targetPrice, PositionType.LongPosition, input.head, Nil),
        executionPrice = targetPrice,
        executionCandle = input.last,
        positionType = PositionType.LongPosition
      ))
    }

    it("does not buy if price is not reached") {
      val input = List(
        CandleStick(DateTime.parse("2017-01-01T01:00:00Z").toInstant, 1.10011, 1.10021, 1.10007, 1.10012, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:05:00Z").toInstant, 1.10009, 1.10019, 1.10005, 1.10010, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:10:00Z").toInstant, 1.10007, 1.10017, 1.10003, 1.10008, 0, complete = true),
        CandleStick(DateTime.parse("2017-01-01T01:15:00Z").toInstant, 1.10005, 1.10010, 1.10001, 1.10003, 0, complete = true)
      )

      val targetPrice = 1.10000

      val trader = new Trader(0, new TradingModel {
        override val commission: Int = 0

        override def createOrder(candle: CandleStick) = Option(LimitOrder(price = targetPrice, PositionType.LongPosition, candle, Nil))
      })

      input.foreach(trader.processCandles)

      trader.trades shouldBe Nil
      trader.orders shouldBe List(LimitOrder(price = targetPrice, PositionType.LongPosition, input.head, Nil))
      trader.positionOption shouldBe None
    }
  }

}
