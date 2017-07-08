package org.nikosoft.oanda.bot

import akka.actor.{Actor, Props}
import akka.routing.{BroadcastRoutingLogic, Router}

object ManagerActor {
  def instrumentStreamingActorProps(accountId: String, instrument: String) = Props.create(classOf[InstrumentStreamingActor], accountId, instrument)
  def candleStreamingActorProps(accountId: String, instrument: String) = Props.create(classOf[CandleStreamingActor], accountId, instrument)
}

class ManagerActor(accountId: String, instrument: String) extends Actor {
  import ManagerActor._

  def receive = throw new RuntimeException("not supposed to receive messages")

  override def preStart(): Unit = {
    Router(BroadcastRoutingLogic(), /*Vector(ActorRefRoutee())*/)

    val instrumentStreamingActor = context.actorOf(instrumentStreamingActorProps(accountId, instrument))
    val candleStreamingActor = context.actorOf(candleStreamingActorProps(accountId, instrument))
  }
}
