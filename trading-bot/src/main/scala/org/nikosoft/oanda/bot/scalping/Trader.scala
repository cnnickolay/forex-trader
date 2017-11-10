package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.bot.scalping.logic.{StopLossLogic, TakeProfitLogic}
import org.nikosoft.oanda.instruments.Model.CandleStick

class Trader(commission: Int, var tradingModel: TradingModel) {

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
      val trade =
        TakeProfitLogic.takeProfit(commission, candle, position) orElse
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
      val position = Position(
        creationOrder = marketOrder,
        executionPrice = candle.open,
        executionCandle = candle,
        positionType = marketOrder.positionType)
//      println(s"Position type ${position.positionType}, order open price ${position.creationOrder.orderCreatedAt.open}, execution price ${position.executionPrice}, take profit at ${position.creationOrder.findTakeProfitOrder.map(_.takeProfitPrice)}, stop loss at ${position.creationOrder.findStopLossOrder.map(_.stopLossPrice)}")
      Option(position)
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

  def stats = {
    val profitList = trades.map(_.profitPips)
    val positives = profitList.count(_ > 0)
    val totalPositives = profitList.filter(_ > 0).sum
    val negatives = profitList.count(_ < 0)
    val totalNegatives = profitList.filter(_ < 0).sum
    val profit = profitList.sum
    (profit, positives, negatives, totalPositives, totalNegatives)
  }

  def tradesFrom(candle: CandleStick): List[Trade] = trades.filter(_.position.creationOrder.orderCreatedAt.time.isAfter(candle.time))

  def statsString: String = {
    val (profit, positives, negatives, totalPositives, totalNegatives) = stats
    s"Total trades done ${trades.size}, total profit: $profit, positives: $positives ($totalPositives), negatives: $negatives ($totalNegatives)"
  }

}

