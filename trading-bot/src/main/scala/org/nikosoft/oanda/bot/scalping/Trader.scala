package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.bot.scalping.logic.{StopLossLogic, TakeProfitLogic}
import org.nikosoft.oanda.instruments.Model.CandleStick

class Trader(commission: Int, tradingModel: TradingModel) {

  import Model._

  var orders: List[Order] = List.empty
  var positionOption: Option[Position] = None
  var trades: List[Trade] = List.empty

  def processCandles(candle: CandleStick): Unit = (orders, positionOption) match {
    case (Nil, None) => orders = tradingModel.createOrder(candle).toList
    case (order +: Nil, None) =>
      if (tradingModel.cancelOrder(candle, order)) orders = Nil
      else {
        positionOption = executeMarketOrder(candle, order) orElse executeLimitOrder(candle, order)
        if (positionOption.isDefined) {
          orders = orders.filterNot(_ == order) ++ order.chainedOrders
          processCandles(candle)
        }
      }
    case (_, Some(position)) =>
      val trade = TakeProfitLogic.takeProfit(commission, candle, position) orElse
        StopLossLogic.stopLoss(commission, candle, position) orElse
        closePosition(candle, position)
      trade.fold() { trade =>
        trades = trade +: trades
        positionOption = None
        orders = Nil
      }
  }

  def closePosition(candle: CandleStick, position: Position) = {
    if (tradingModel.closePosition(candle, position))
      Option(Trade(
        commissionPips = commission,
        orderClosedAt = candle,
        closedAtPrice = candle.close,
        tradeType = TradeType.ManualClose,
        position = position))
    else Option.empty[Trade]
  }

  def executeMarketOrder(candle: CandleStick, order: Order): Option[Position] = order match {
    case marketOrder: MarketOrder =>
      Option(Position(
        creationOrder = marketOrder,
        executionPrice = candle.open,
        executionCandle = candle,
        positionType = marketOrder.positionType))
    case _ => None
  }

  def executeLimitOrder(candle: CandleStick, order: Order): Option[Position] = order match {
    case limitOrder: LimitOrder =>
      val position = Position(
        creationOrder = limitOrder,
        executionPrice = limitOrder.price,
        executionCandle = candle,
        positionType = limitOrder.positionType)

      if (limitOrder.positionType == PositionType.LongPosition && candle.low <= limitOrder.price) {
        Option(position)
      } else if (limitOrder.positionType == PositionType.ShortPosition && candle.high >= limitOrder.price) {
        Option(position)
      } else None
    case _ => None
  }

/*  private def buyAtOpenPrice(candles: List[CandleStick], order: Order) = {
    val candle = candles.head
    val takeProfitRate = candle.open + order.takeProfit.abs.toRate * (if (order.orderType == ShortOrderType) -1 else 1)
    val stopLossRate = candle.open + order.stopLoss.toRate * (if (order.orderType == ShortOrderType) 1 else -1)

    orderOption = Option(order.copy(
      boughtAtCandle = Some(candle),
      openAtPrice = Some(candle.open),
      orderState = ExecutedOrder,
      takeProfitAtPrice = Option(takeProfitRate),
      stopLossAtPrice = Option(stopLossRate)
    ))

    processExecutedOrder(candles, order)
  }

  private def processExecutedOrder(candles: List[CandleStick], order: Order) = {
    takeProfit(candles.head)
    stopLoss(candles.head)
    tradingModel.closeOrder(candles, order).fold() { order =>
      orders = order +: orders
      orderOption = None
    }
  }

  def stats = {
    val profitList = orders.map(_.profitPips)
    val positives = profitList.count(_ > 0)
    val totalPositives = profitList.filter(_ > 0).sum
    val negatives = profitList.count(_ < 0)
    val totalNegatives = profitList.filter(_ < 0).sum
    val profit = profitList.sum
    (profit, positives, negatives, totalPositives, totalNegatives)
  }*/

/*
  def statsString: String = {
    val (profit, positives, negatives, totalPositives, totalNegatives) = stats
    s"Total trades done ${orders.size}, total profit: $profit, positives: $positives ($totalPositives), negatives: $negatives ($totalNegatives)"
  }
*/

}

