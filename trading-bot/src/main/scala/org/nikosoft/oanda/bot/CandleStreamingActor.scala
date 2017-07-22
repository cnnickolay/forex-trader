package org.nikosoft.oanda.bot

import akka.actor.{Actor, ActorRef}
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.{Candlestick, CandlestickData}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.bot.CandleStreamingActor.Tick
import org.nikosoft.oanda.instruments.Model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationLong
import scalaz.Scalaz._

object CandleStreamingActor {
  case object Tick
}

class CandleStreamingActor(next: ActorRef, chart: Chart) extends Actor {
  override def preStart() = {
    context.system.scheduler.schedule(0.seconds, 10.seconds, self, Tick)
  }

  def receive = {
    case Tick =>
      val candlesResponse = Api.instrumentsApi
        .candles(
          instrument = InstrumentName(chart.instrument),
          granularity = chart.granularity,
          count = (chart._candles.isEmpty ? 5000 | 2).some
//          count = None,
//          from = Some(DateTime("2017-07-16T00:00:00Z")),
//          to = Some(DateTime("2017-07-17T00:00:00Z"))
        )

      candlesResponse.map(_.candles
        .flatMap(candle => candle.mid.map(toCandleStick(candle, _)))
        .filter(_.complete)
        .flatMap(chart.addCandleStick)
      ).fold(err => println(err), {
        case lastCandle +: Nil => next ! lastCandle
        case candles @ lastCandle +: tail => next ! chart._candles
        case _ =>
      })

  }

  def toCandleStick(candle: Candlestick, c: CandlestickData): CandleStick = CandleStick(candle.time.toInstant, c.o.value, c.h.value, c.l.value, c.c.value, candle.volume, candle.complete)
}
