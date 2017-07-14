package org.nikosoft.oanda.bot

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.nikosoft.oanda.bot.TrainerActor.Statistics
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scalaz.Scalaz._

object TrainerActor {
  case class Statistics(maxAtr: Int, minAtr: Int)
}

class TrainerActor extends Actor {

  def receive = {
    case candles: Seq[CandleStick] =>
      val macd = candles.flatMap(_.indicators.get("MACDCandleCloseIndicator")).map(_.asInstanceOf[MACDItem])
//      println(Statistics(rsi.max.toInt, rsi.min.toInt))

      println("----- going short")
      val shortResults = macd.sliding(3)
        .filter { case (_ +: second +: third +: _) => ~second.histogram < 0 && ~third.histogram > 0 }
        .map { case (first +: second +: _) => first.price - second.price }
        .toList

      println(s"Success rate ${((shortResults.count(_ < 0).toDouble / shortResults.size) * 100).toInt}%")

      println("----- going long")
      val longResults = macd.sliding(3)
        .filter { case (_ +: second +: third +: _) => ~second.histogram > 0 && ~third.histogram < 0 }
        .map { case (first +: second +: _) => first.price - second.price }
        .toList
      println(s"Success rate ${((longResults.count(_ > 0).toDouble / longResults.size) * 100).toInt}%")

      candles.foreach(c => {
        println(c.close)
        c.indicators.foreach(i => println(s"  $i"))
      })
  }

}
