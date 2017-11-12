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

object PriceScalper extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  implicit val formats = JsonSerializers.formats

  val consumerSettings = ConsumerSettings(actorSystem, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("client")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  var prices = Vector.empty[Price]

  Consumer.committableSource(consumerSettings, Subscriptions.topics("prices"))
    .mapConcat { record =>
      val value = record.record.value()
      val price = read[Price](value)
      //      if (price.time.isAfter(DateTime.now.minusHours(12))) {
      List(price)
      //      } else Nil
    }
    .runWith(Sink.foreach(calculateSlope))

  var lastDiverge = Option.empty[BigDecimal]
  var positionOpenedAt = Option.empty[BigDecimal]
  val buySellDiverge = 50
  val defaultTakeProfit = 30
  val secondsToCalculateAverage = 60 * 60

  object PositionType extends Enumeration {
    type PositionType = Value
    val LongPosition, ShortPosition = Value
  }

  implicit class PricePimped(price: Price) {
    def avgPrice: BigDecimal = (price.bids.head.price.value + price.asks.head.price.value) / 2
  }

  case class Order(creationPrice: BigDecimal, creationDate: DateTime, takeProfit: Int, positionType: PositionType, closedPrice: Option[Price] = None, closedAt: Option[DateTime] = None) {
    def profit: Int = closedPrice.fold(0) { closePrice =>
      if (positionType == LongPosition) ((closePrice.closeoutBid.value - creationPrice) * 100000).toInt else ((creationPrice - closePrice.closeoutAsk.value) * 100000).toInt
    }

    def closeOrder(currentPrice: Price): Boolean =
      if (positionType == LongPosition && ((currentPrice.closeoutBid.value - creationPrice) * 100000).toInt >= takeProfit) true
      else if (positionType == ShortPosition && ((creationPrice - currentPrice.closeoutAsk.value) * 100000).toInt >= takeProfit) true
      else false

    def closedAtPrice: BigDecimal = ~closedPrice.map(price => (positionType == LongPosition) ? price.closeoutBid.value | price.closeoutAsk.value)

    def orderDuration = ~closedAt.map(closeDate => new Duration(creationDate, closeDate).getStandardHours)
  }

  var openOrders: List[Order] = Nil
  var closedOrders: List[Order] = Nil

  def calculateSlope(price: Price): Unit = {
    prices = price +: prices
    val lastPrice: Price = prices.head
    val fromPrice = lastPrice.time.minusSeconds(secondsToCalculateAverage)
    val values = prices.filter(_.time.isAfter(fromPrice))
    val pairs = values
      .map(price => {
        val normPrice = (price.asks.head.price.value.toDouble + price.bids.head.price.value.toDouble) / 2
        normPrice
      })
      .sum / values.length
    val diverge = (lastPrice.avgPrice - pairs) * 100000
    if (lastDiverge.isEmpty && diverge.abs > buySellDiverge && openOrders.isEmpty) {
      val positionType = if (diverge > 0) LongPosition else ShortPosition
      val order = Order((positionType == LongPosition) ? lastPrice.asks.head.price.value | lastPrice.bids.head.price.value, lastPrice.time, defaultTakeProfit, positionType, None)
//      println(s"Opening order $lastPrice")
      openOrders = order +: openOrders
    }

    val ordersToClose = openOrders.filter(_.closeOrder(lastPrice))
    val justClosedOrders = ordersToClose.map(_.copy(closedPrice = Option(lastPrice), closedAt = Option(lastPrice.time)))
    closedOrders = closedOrders ++ justClosedOrders
    openOrders = openOrders.filterNot(ordersToClose.contains)

    justClosedOrders.foreach { order =>
//      println(s"Closing order $lastPrice")
      println(s"Closed ${order.positionType}, opened at ${order.creationPrice}, closed at ${order.closedAtPrice}, profit ${order.profit}, time ${order.orderDuration}, total profit ${closedOrders.map(_.profit).sum}")
    }
  }

}
