package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef}
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity.CandlestickGranularity
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.{Candlestick, CandlestickData}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.bot.CandleStreamingActor.Tick
import org.nikosoft.oanda.instruments.Model._

import scala.concurrent.duration.DurationLong
import scalaz.Scalaz._

object CandleStreamingActor {

  case object Tick

}

class CandleStreamingActor(next: ActorRef, accountId: String, instrument: String, granularity: CandlestickGranularity) extends Actor {

  val chart = new Chart(indicators = Seq(new MACDCandleCloseIndicator(), new RSICandleCloseIndicator(14), new EMACandleCloseIndicator(50)))

  var candles: Seq[CandleStick] = Seq.empty

  override def preStart() = {
    context.system.scheduler.schedule(0.seconds, 10.seconds, self, Tick)
  }

  def receive = {
    case Tick =>
      val candlesResponse = Api.instrumentsApi
        .candles(
          instrument = InstrumentName(instrument),
          granularity = granularity,
          count = (candles.isEmpty ? 500 | 2).some
        )

      candlesResponse.map(_.candles
        .flatMap(candle => candle.mid.map(toCandleStick(candle, _)))
        .find(_.complete).foreach(next ! chart.addCandleStick(_))
      )

  }

  def toCandleStick(candle: Candlestick, c: CandlestickData): CandleStick = CandleStick(candle.time.toInstant, c.o.value, c.h.value, c.l.value, c.c.value, candle.volume, candle.complete)
}
