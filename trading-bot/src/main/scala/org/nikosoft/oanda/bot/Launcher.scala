package org.nikosoft.oanda.bot

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.PricingModel.Price
import org.nikosoft.oanda.bot.SimpleCommands.{Start, Stop}

class PriceConsumer extends Actor {
  def receive: Receive = {
    case price: Price => println(price)
  }
}

object Launcher extends App {

  val actorSystem = ActorSystem("bot")

  val temp = actorSystem.actorOf(Props[PriceConsumer], "temp")
  val traderActor = actorSystem.actorOf(Props.create(classOf[TraderActor], "001-004-1442547-003", "EUR_USD"), "trader-actor")
  val instrumentActor = actorSystem.actorOf(Props.create(classOf[InstrumentStreamingActor], "001-004-1442547-003", "EUR_USD", traderActor), "instrument-streaming")

  instrumentActor ! Start
}
