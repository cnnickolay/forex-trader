package org.nikosoft.oanda.bot.pricescalper

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.json4s.jackson.Serialization.read
import org.nikosoft.oanda.api.ApiModel.PricingModel.Price
import org.nikosoft.oanda.api.JsonSerializers

object PriceScalper extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  implicit val formats = JsonSerializers.formats

  val consumerSettings = ConsumerSettings(actorSystem, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("client")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  var prices = Vector.empty[Price]
  var slopes = List.empty[Double]

  Consumer.committableSource(consumerSettings, Subscriptions.topics("prices"))
    .map { record =>
      val value = record.record.value()
      read[Price](value)
    }
    .runWith(Sink.foreach(calculateSlope))

  def calculateSlope(price: Price): Unit = {
    prices = price +: prices
    val lastPrice = prices.head
    val fromPrice = lastPrice.time.minusSeconds(60 * 60)
    val values = prices.filter(_.time.isAfter(fromPrice))
    val pairs = values
      .map(price => {
        val normPrice = (price.asks.head.price.value.toDouble + price.bids.head.price.value.toDouble) / 2
        normPrice
      })
      .sum / values.length
    val avg = (lastPrice.bids.head.price.value + lastPrice.asks.head.price.value) / 2
    println(f"Time ${lastPrice.time}, bid ${lastPrice.bids.head.price.value}, asks ${lastPrice.asks.head.price.value}, slope $pairs%1.5f, ${(avg - pairs) * 100000}%1.0f")
  }

}
