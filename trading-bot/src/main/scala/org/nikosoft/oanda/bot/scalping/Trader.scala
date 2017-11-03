package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.instruments.Model.CandleStick

class Trader(tradingModel: TradingModel) {

  import Model._

  var currentOrderOption: Option[Order] = None
  var orders: List[Order] = List.empty

  def processCandles(candles: List[CandleStick]): Unit = currentOrderOption match {
    case None => currentOrderOption = tradingModel.openOrder(candles)
    case Some(order) if order.orderState == PendingOrder =>
      val candle = candles.head
      val takeProfitRate = candle.open + order.takeProfit.abs.toRate * (if (order.orderType == ShortOrderType) -1 else 1)
      val stopLossRate = candle.open + order.stopLoss.toRate * (if (order.orderType == ShortOrderType) 1 else -1)

      currentOrderOption = Option(order.copy(
        boughtAtCandle = Some(candle),
        openAtPrice = Some(candle.open),
        orderState = ExecutedOrder,
        takeProfitAtPrice = Option(takeProfitRate),
        stopLossAtPrice = Option(stopLossRate)
      ))

      processExecutedOrder(candles, order)
    case Some(order) if order.orderState == ExecutedOrder => processExecutedOrder(candles, order)
  }

  private def processExecutedOrder(candles: List[CandleStick], order: Order) = {
    takeProfit(candles.head)
    stopLoss(candles.head)
    tradingModel.closeOrder(candles, order).fold() { order =>
      orders = order +: orders
      currentOrderOption = None
    }
  }

  def takeProfit(candle: CandleStick) = for {
    order <- currentOrderOption
    takeProfitAtPrice <- order.takeProfitAtPrice
  } yield if (
    (order.orderType == LongOrderType && takeProfitAtPrice <= candle.high) ||
    (order.orderType == ShortOrderType && takeProfitAtPrice >= candle.low)
  ) {
    orders = order.copy(orderState = TakeProfitOrder, closedAtPrice = takeProfitAtPrice, closedAtCandle = Some(candle)) +: orders
    currentOrderOption = None
  }

  def stopLoss(candle: CandleStick) = for {
    order <- currentOrderOption
    stopLossAtPrice <- order.stopLossAtPrice
  } yield if (
    (order.orderType == LongOrderType && stopLossAtPrice >= candle.low) ||
    (order.orderType == ShortOrderType && stopLossAtPrice <= candle.high)
  ) {
    orders = order.copy(orderState = StopLossOrder, closedAtPrice = stopLossAtPrice, closedAtCandle = Some(candle)) +: orders
    currentOrderOption = None
  }

  def stats = {
    val profitList = orders.map(_.profitPips)
    val positives = profitList.count(_ > 0)
    val totalPositives = profitList.filter(_ > 0).sum
    val negatives = profitList.count(_ < 0)
    val totalNegatives = profitList.filter(_ < 0).sum
    val profit = profitList.sum
    (profit, positives, negatives, totalPositives, totalNegatives)
  }

  def statsString: String = {
    val (profit, positives, negatives, totalPositives, totalNegatives) = stats
    s"Total trades done ${orders.size}, total profit: $profit, positives: $positives ($totalPositives), negatives: $negatives ($totalNegatives)"
  }

}

