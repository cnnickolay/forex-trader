package org.nikosoft.oanda.bot

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity
import org.nikosoft.oanda.instruments.Model.{StochasticCandleIndicator, _}

object Launcher extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val source = new Chart(
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
      new StochasticCandleIndicator(5, Some(3), Some(3))
    )
  ).streamCsv("/Users/niko/projects/oanda-trader/data/EURUSD.csv", ";")

  val sink = Sink.foreach[CandleStick](_ => {})

  source.runWith(sink)

//  val managerActor = actorSystem.actorOf(Props.create(classOf[ManagerActor], chart), "manager-actor")
}
