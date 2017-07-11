package org.nikosoft.oanda.bot

import akka.actor.{ActorSystem, Props}
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity
import org.nikosoft.oanda.instruments.Model.{Chart, EMACandleCloseIndicator, MACDCandleCloseIndicator, RSICandleCloseIndicator}

object Launcher extends App {

  val actorSystem = ActorSystem("bot")

  val chart = new Chart(
    accountId = "001-004-1442547-003",
    instrument = "EUR_USD",
    granularity = CandlestickGranularity.M5,
    indicators = Seq(
      new MACDCandleCloseIndicator(),
      new RSICandleCloseIndicator(14),
      new EMACandleCloseIndicator(50)
    )
  )
  val managerActor = actorSystem.actorOf(Props.create(classOf[ManagerActor], chart), "manager-actor")

}
