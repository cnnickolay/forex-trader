package org.nikosoft.oanda.bot

import akka.actor.{ActorSystem, Props}
import org.nikosoft.oanda.bot.CommonCommands.StartActor

object Launcher extends App {

  val actorSystem = ActorSystem("bot")


  val managerActor = actorSystem.actorOf(Props.create(classOf[ManagerActor], "001-004-1442547-003", "EUR_USD"), "manager-actor")

  managerActor ! StartActor
}
