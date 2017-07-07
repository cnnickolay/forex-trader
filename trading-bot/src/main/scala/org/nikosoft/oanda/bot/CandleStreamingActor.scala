package org.nikosoft.oanda.bot

import akka.actor.Actor
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.{Candlestick, CandlestickData}
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity.CandlestickGranularity
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.bot.CandleStreamingActor.Tick
import org.nikosoft.oanda.instruments.Model.CandleStick

import scala.concurrent.duration.DurationLong
import scalaz.Scalaz._

object CandleStreamingActor {
  case object Tick
}

class CandleStreamingActor(accountId: String, instrument: String, granularity: CandlestickGranularity) extends Actor {

  var candles: Seq[CandleStick] = Seq.empty

  override def preStart() = {
    context.system.scheduler.schedule(0.seconds, 10.seconds, self, Tick)
  }

  def receive = {
    case Tick =>
      val lastCandles = Api.instrumentsApi.candles(
        instrument = InstrumentName(instrument),
        granularity = granularity,
        count = (candles.isEmpty ? 500 | 2).some
      ).map(_.candles).map(_.flatMap(candle => candle.mid.map(toCandleStick(candle, _))))
  }

  def toCandleStick(candle: Candlestick, c: CandlestickData): CandleStick = CandleStick(candle.time.toInstant, c.o.value, c.h.value, c.l.value, c.c.value, candle.volume, candle.complete)
}
