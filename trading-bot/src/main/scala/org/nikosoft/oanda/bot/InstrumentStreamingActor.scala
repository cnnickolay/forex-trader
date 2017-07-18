package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef, PoisonPill}
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.bot.CommonCommands.{StartActor, StopActor}
import org.nikosoft.oanda.instruments.Model.Chart

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InstrumentStreamingActor(next: ActorRef, chart: Chart) extends Actor {

  var terminate = false

  def receive: Receive = {
    case StartActor =>
      Future {
        val stream = Api.pricingApi.pricingStream(AccountID(chart.accountId), Seq(InstrumentName(chart.instrument)), snapshot = true, terminate = terminate)
        Iterator.continually(stream.take()).foreach(_.foreach(next ! _))
      }
    case StopActor =>
      println("Shutting down")
      terminate = true
      self ! PoisonPill
    case _ => println("This actor should not receive any messages")
  }

}
