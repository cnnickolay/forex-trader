package org.nikosoft.oanda.bot

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.nikosoft.oanda.bot.TrainerActor.Statistics
import org.nikosoft.oanda.instruments.Model.{CandleStick, EMACandleCloseIndicator, StochasticCandleIndicator}
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scalaz.Scalaz._

object TrainerActor {

  case class Statistics(maxAtr: Int, minAtr: Int)

}

class TrainerActor extends Actor {

  def receive = {
    case candles: Seq[CandleStick] =>
      (3 to 20).foreach(assess(candles, _))
  }


  def assess(candles: Seq[CandleStick], range: Int): Unit = {
    val trades = candles.sliding(range)
      .flatMap { case (current +: _ :+ second :+ third) =>
        val secondEma50Option = second.indicator[EMACandleCloseIndicator, BigDecimal]("50")
        val secondEma100Option = second.indicator[EMACandleCloseIndicator, BigDecimal]("100")
        val secondStochasticOption = second.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
        val thirdStochasticOption = third.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")

        val longTrade = for {
          secondEma50 <- secondEma50Option
          secondEma100 <- secondEma100Option if secondEma50 > secondEma100
          secondStochastic <- secondStochasticOption
          thirdStochastic <- thirdStochasticOption if thirdStochastic < 20 && secondStochastic > 20
        } yield ((current.close - second.close) * 100000).toInt

        val shortTrade = for {
          secondEma50 <- secondEma50Option
          secondEma100 <- secondEma100Option if secondEma50 < secondEma100
          secondStochastic <- secondStochasticOption
          thirdStochastic <- thirdStochasticOption if thirdStochastic > 80 && secondStochastic < 80
        } yield ((current.close - second.close) * 100000).toInt * -1

        (longTrade, shortTrade) match {
          case (Some(long), None) => Some(long)
          case (None, Some(short)) => Some(short)
          case _ => None
        }
      }.toList

//    trades.foreach(println)
    println(s"$range minutes passed after order, profit ${trades.filter(_ > 0).sum}, loss ${trades.filter(_ < 0).sum}")
  }
}
