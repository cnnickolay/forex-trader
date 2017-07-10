package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef, Props}
import org.nikosoft.oanda.instruments.Model.Chart

object ManagerActor {
  def instrumentStreamingActorProps(next: ActorRef, chart: Chart) = Props.create(classOf[InstrumentStreamingActor], next, chart: Chart)
  def candleStreamingActorProps(next: ActorRef, chart: Chart) = Props.create(classOf[CandleStreamingActor], next, chart: Chart)
  def advisorActorProps(chart: Chart) = Props.create(classOf[AdvisorActor], chart)
}

class ManagerActor(chart: Chart) extends Actor {
  import ManagerActor._

  def receive = {
    case unexpectedEvent => throw new RuntimeException(s"not supposed to receive messages, but has received $unexpectedEvent")
  }

  override def preStart(): Unit = {
    val advisorActor = context.actorOf(advisorActorProps(chart))

//    val instrumentStreamingActor = context.actorOf(instrumentStreamingActorProps(advisorActor, accountId, instrument))
    val candleStreamingActor = context.actorOf(candleStreamingActorProps(advisorActor, chart))
  }
}
