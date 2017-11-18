package org.nikosoft.oanda.bot.pricescalper

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.joda.time.{DateTime, Duration}
import org.json4s.jackson.Serialization.read
import org.nikosoft.oanda.api.ApiModel.PricingModel.Price
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.bot.pricescalper.PriceScalper.PositionType.{LongPosition, PositionType, ShortPosition}

import scalaz.Scalaz._
import org.nikosoft.oanda.bot.scalping.Model._

object PriceScalper extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  implicit val formats = JsonSerializers.formats

  val consumerSettings = ConsumerSettings(actorSystem, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("client")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  //  var prices: Vector[Price] = Vector.empty

  val sink = Sink.fold[List[Price], Price](Nil)((prices, price) => {
    val updatedPrices = price +: prices
    calculateSlope(updatedPrices)
    updatedPrices
  })

  var prices: Vector[Price] = Vector.empty

  Consumer.committableSource(consumerSettings, Subscriptions.topics("prices"))
    .map { record =>
      val value = record.record.value()
      val price = read[Price](value)
      prices = price +: prices
//      if (price.time.hourOfDay.get >= 6 && price.time.hourOfDay.get <= 14)
        calculateSlope(prices)
/*
      val first = prices.take(2)
      if (first.length == 2 && first.head.time.getDayOfMonth != first.last.time.getDayOfMonth) {
        println(price)
      }
*/
      //      price.time.hourOfDay()
//      println(price.time)
//      if (price.time.isAfter(DateTime.now.minusHours(24)) && price.time.hourOfDay.get == 6) {
//      } else Nil
    }
    .runWith(Sink.ignore)

  object PositionType extends Enumeration {
    type PositionType = Value
    val LongPosition, ShortPosition = Value
  }

  def meanPrice(prices: List[Price]): BigDecimal = prices.map(price => (price.bids.head.price.value + price.asks.head.price.value) / 2).sum / prices.length

  def meanPrice(price: Price): BigDecimal = (price.bids.head.price.value + price.asks.head.price.value) / 2

  case class Order(creationPrice: BigDecimal, creationDate: DateTime, takeProfit: Int, positionType: PositionType, closedPrice: Option[Price] = None, closedAt: Option[DateTime] = None) {
    def profit: Int = ~closedPrice.map(profit)

    def profit(currentPrice: Price): Int = if (positionType == LongPosition)
      (currentPrice.closeoutBid.value - creationPrice).toPips else (creationPrice - currentPrice.closeoutAsk.value).toPips

    def closeOrder(currentPrice: Price): Boolean = if (profit(currentPrice) >= takeProfit) true else false

    def closedAtPrice: BigDecimal = ~closedPrice.map(price => (positionType == LongPosition) ? price.closeoutBid.value | price.closeoutAsk.value)

    def orderDuration(currentTime: DateTime): Long = new Duration(creationDate, currentTime).getStandardMinutes
    def orderDuration: Long = ~closedAt.map(orderDuration)
  }

  val divergenceThreshold = 70
  val distanceBetweenPrices = 5
  val defaultTakeProfit = 30
  val defaultStopLoss = -100
  val secondsToCalculateAverage = 2
  val maxMinutesToWait = 2

  var openOrders: List[Order] = Nil
  var closedOrders: List[Order] = Nil

  def avg(price: Price): BigDecimal = (price.asks.head.price.value + price.bids.head.price.value) / 2
  def pips(value: BigDecimal) = value * 100000
  def spread(price: Price): Int = ((price.asks.head.price.value - price.bids.head.price.value) * 100000).toInt.abs

  def calculateSlope(prices: Seq[Price]): Unit = {
    val lastPrice: Price = prices.head
    val fromTime = lastPrice.time.minusSeconds(secondsToCalculateAverage)
    val (values, _) = prices.partition(_.time.isAfter(fromTime))
    if (openOrders.isEmpty && values.length >= 2 && spread(lastPrice) <= 15) {
      val diverge = pips(avg(values.head) - avg(values.last))
      def positives = values.sliding(2).forall { case now +: past +: _ => pips(avg(now) - avg(past)) >= distanceBetweenPrices } && diverge >= divergenceThreshold
      def negatives = values.sliding(2).forall { case now +: past +: _ => pips(avg(past) - avg(now)) >= distanceBetweenPrices } && diverge <= divergenceThreshold
      val positionTypeOption = if (positives) Option(ShortPosition) else if (negatives) Option(LongPosition) else None

      openOrders = positionTypeOption.map { positionType =>
        val order = Order((positionType == LongPosition) ? lastPrice.asks.head.price.value | lastPrice.bids.head.price.value, lastPrice.time, defaultTakeProfit, positionType, None)
        println(s"Opened $positionType order at ${lastPrice.time}")
        order
      }.toList ++ openOrders
    }
    if (openOrders.nonEmpty) {
      val ordersToClose = openOrders.filter(_.closeOrder(lastPrice))
      if (ordersToClose.nonEmpty) {
        val justClosedOrders = ordersToClose.map(_.copy(closedPrice = Option(lastPrice), closedAt = Option(lastPrice.time)))
        closedOrders = closedOrders ++ justClosedOrders
        openOrders = openOrders.filterNot(ordersToClose.contains)

        justClosedOrders.foreach { order =>
          println(s"Take profit ${order.positionType}, open price ${order.creationPrice} at ${order.creationDate}, close price ${order.closedAtPrice} at ${order.closedAt.getOrElse("")}, profit ${order.profit}, duration ${order.orderDuration} minutes, total profit ${closedOrders.map(_.profit).sum}")
        }
      }

      def staleOrders = openOrders.filter(_.orderDuration(lastPrice.time) >= maxMinutesToWait).toSet
      def stopLoss = openOrders.filter(_.profit(lastPrice) < defaultStopLoss).toSet
      val outdatedOrders: Set[Order] = staleOrders ++ stopLoss
      if (outdatedOrders.nonEmpty) {
        val justClosedOrders = outdatedOrders.map(_.copy(closedPrice = Option(lastPrice), closedAt = Option(lastPrice.time)))
        closedOrders = closedOrders ++ justClosedOrders
        openOrders = openOrders.filterNot(outdatedOrders.contains)

        justClosedOrders.foreach { order =>
          println(s"Stop loss ${order.positionType}, open price ${order.creationPrice} at ${order.creationDate}, close price ${order.closedAtPrice} at ${order.closedAt.getOrElse("")}, profit ${order.profit}, duration ${order.orderDuration} minutes, total profit ${closedOrders.map(_.profit).sum}")
        }
      }
    }
  }

}
