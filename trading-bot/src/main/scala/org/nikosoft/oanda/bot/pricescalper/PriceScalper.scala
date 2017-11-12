package org.nikosoft.oanda.bot.pricescalper

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
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

  Consumer.committableSource(consumerSettings, Subscriptions.topics("prices"))
    .mapConcat { record =>
      val value = record.record.value()
      val price = read[Price](value)
      //      if (price.time.isAfter(DateTime.now.minusHours(12))) {
      List(price)
      //      } else Nil
    }
    .runWith(sink)

  object PositionType extends Enumeration {
    type PositionType = Value
    val LongPosition, ShortPosition = Value
  }

  implicit class PricesPimped(prices: List[Price]) {
    def meanPrice: BigDecimal = prices.map(price => (price.bids.head.price.value + price.asks.head.price.value) / 2).sum / prices.length
  }

  implicit class PricePimped(price: Price) {
    def meanPrice: BigDecimal = (price.bids.head.price.value + price.asks.head.price.value) / 2
  }

  def meanPrice(prices: List[Price]): BigDecimal = prices.map(price => (price.bids.head.price.value + price.asks.head.price.value) / 2).sum / prices.length
  def meanPrice(price: Price): BigDecimal = (price.bids.head.price.value + price.asks.head.price.value) / 2

  case class Order(creationPrice: BigDecimal, creationDate: DateTime, takeProfit: Int, positionType: PositionType, closedPrice: Option[Price] = None, closedAt: Option[DateTime] = None) {
    def profit: Int = ~closedPrice.map(profit)

    def profit(currentPrice: Price): Int = if (positionType == LongPosition)
      (currentPrice.closeoutBid.value - creationPrice).toPips else (creationPrice - currentPrice.closeoutAsk.value).toPips

    def closeOrder(currentPrice: Price): Boolean = if (profit(currentPrice) >= takeProfit) true else false

    def closedAtPrice: BigDecimal = ~closedPrice.map(price => (positionType == LongPosition) ? price.closeoutBid.value | price.closeoutAsk.value)

    def orderDuration = ~closedAt.map(closeDate => new Duration(creationDate, closeDate).getStandardHours)
  }

  val divergenceThreshold = 50
  val defaultTakeProfit = 50
  val secondsToCalculateAverage = 60 * 60

  var openOrders: List[Order] = Nil
  var closedOrders: List[Order] = Nil

  def calculateSlope(prices: Seq[Price]): Unit = {
    val lastPrice: Price = prices.head
    val fromTime = lastPrice.time.minusSeconds(secondsToCalculateAverage)
    val (values, _) = prices.partition(_.time.isAfter(fromTime))
    val averagePrice = values
      .map(price => {
        val normPrice = (price.asks.head.price.value.toDouble + price.bids.head.price.value.toDouble) / 2
        normPrice
      })
      .sum / values.length
    val diverge = (meanPrice(lastPrice) - averagePrice).toPips
    if (diverge.abs > divergenceThreshold && openOrders.isEmpty) {
      val positionType = if (diverge > 0) LongPosition else ShortPosition
      val order = Order((positionType == LongPosition) ? lastPrice.asks.head.price.value | lastPrice.bids.head.price.value, lastPrice.time, defaultTakeProfit, positionType, None)
      openOrders = order +: openOrders
    }

    val ordersToClose = openOrders.filter(_.closeOrder(lastPrice))
    val justClosedOrders = ordersToClose.map(_.copy(closedPrice = Option(lastPrice), closedAt = Option(lastPrice.time)))
    closedOrders = closedOrders ++ justClosedOrders
    openOrders = openOrders.filterNot(ordersToClose.contains)

    justClosedOrders.foreach { order =>
      println(s"Closed ${order.positionType}, open price ${order.creationPrice} at ${order.creationDate}, close price ${order.closedAtPrice} at ${order.closedAt.getOrElse("")}, profit ${order.profit}, duration ${order.orderDuration} hours, total profit ${closedOrders.map(_.profit).sum}")
    }
  }

}
