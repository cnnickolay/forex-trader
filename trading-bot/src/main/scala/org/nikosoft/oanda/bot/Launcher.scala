package org.nikosoft.oanda.bot

import akka.actor.{ActorSystem, Props}
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity
import org.nikosoft.oanda.instruments.Model.{StochasticCandleIndicator, _}

object Launcher extends App {

  val actorSystem = ActorSystem("bot")

  val chart = new Chart(
    accountId = "001-004-1442547-003",
    instrument = "EUR_USD",
    granularity = CandlestickGranularity.M1,
    indicators = Seq(
      new MACDCandleCloseIndicator(),
      new RSICandleCloseIndicator(14),
      new EMACandleCloseIndicator(50),
      new EMACandleCloseIndicator(100),
      new ATRCandleIndicator(14),
      new CMOCandleCloseIndicator(21),
      new StochasticCandleIndicator(20, Some(3), Some(3))
    )
     /*++ (for {
      range <- 3 to 7
      smoothing <- 3 to 4
      secondSmoothing <- 3 to 4
    } yield new StochasticCandleIndicator(range, Some(smoothing), Some(secondSmoothing)))*/
  )

  val managerActor = actorSystem.actorOf(Props.create(classOf[ManagerActor], chart), "manager-actor")
}
