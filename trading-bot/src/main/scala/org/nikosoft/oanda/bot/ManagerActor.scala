package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef, Props}
import org.nikosoft.oanda.bot.CommonCommands.StartActor
import org.nikosoft.oanda.bot.scalpers.{MACDScalperActor, ScalperActor}
import org.nikosoft.oanda.instruments.Model.Chart

object ManagerActor {
  def instrumentStreamingActorProps(next: ActorRef, chart: Chart) = Props.create(classOf[InstrumentStreamingActor], next, chart: Chart)
  def candleStreamingActorProps(next: ActorRef, chart: Chart) = Props.create(classOf[CandleStreamingActor], next, chart: Chart)
  def advisorActorProps(chart: Chart) = Props.create(classOf[AdvisorActor], chart)
  def scalperActorProps(chart: Chart) = Props.create(classOf[ScalperActor], chart)
  def macdScalperActorProps(chart: Chart) = Props.create(classOf[MACDScalperActor], chart)
}

class ManagerActor(chart: Chart) extends Actor {
  import ManagerActor._

  def receive = {
    case unexpectedEvent => throw new RuntimeException(s"not supposed to receive messages, but has received $unexpectedEvent")
  }

  override def preStart(): Unit = {
    val advisorActor = context.actorOf(advisorActorProps(chart))
    val trainerActor = context.actorOf(scalperActorProps(chart))
    val macdScalperActor = context.actorOf(macdScalperActorProps(chart))

    context.actorOf(instrumentStreamingActorProps(macdScalperActor, chart)) ! StartActor
    context.actorOf(candleStreamingActorProps(macdScalperActor, chart))
  }
}
