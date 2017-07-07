package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}

object ManagerActor {
  def instrumentStreamingActorProps(accountId: String, instrument: String) = Props.create(classOf[InstrumentStreamingActor], accountId, instrument)
}

class ManagerActor(accountId: String, instrument: String) extends Actor {
  import ManagerActor._

  def receive = throw new RuntimeException("not supposed to receive messages")

  override def preStart(): Unit = {
    val instrumentStreamingActor: ActorRef = context.actorOf(instrumentStreamingActorProps(accountId, instrument))
    Router(BroadcastRoutingLogic(), Vector(ActorRefRoutee(instrumentStreamingActor)))
  }
}
