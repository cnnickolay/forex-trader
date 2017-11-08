package org.nikosoft.oanda.bot

import org.apache.commons.math3.stat.regression.SimpleRegression

object Main extends App {

  val reg = new SimpleRegression(false)

  (0 until 10 by 2).foreach { idx =>
    reg.addData(idx, idx * 3)
  }

  println(reg.getSlope)

}
