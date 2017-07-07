package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef, PoisonPill}
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.bot.CommonCommands.{StartActor, StopActor}

import scala.concurrent.Future
import scalaz.{-\/, \/-}
import scala.concurrent.ExecutionContext.Implicits.global

class InstrumentStreamingActor(accountId: String, instrument: String, receiver: ActorRef) extends Actor {

  var terminate = false

  def receive: Receive = {
    case StartActor =>
      Future {
        val stream = Api.pricingApi.pricingStream(AccountID(accountId), Seq(InstrumentName(instrument)), snapshot = true, terminate = terminate)
        Iterator.continually(stream.take()).foreach {
          case \/-(price) => receiver ! price
          case -\/(_) =>
        }
      }
    case StopActor =>
      println("Shutting down")
      terminate = true
      self ! PoisonPill
    case _ => println("This actor should not receive any messages")
  }

}
