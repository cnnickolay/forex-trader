package org.nikosoft.oanda.bot.ml

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import org.scalatest.FunSuite

class Main$Test extends FunSuite {

  ignore("test") {
    implicit val actorSystem = ActorSystem("test")
    implicit val actorMaterializer = ActorMaterializer()

    val start = LocalDateTime.of(2010, 1, 1, 0, 0, 0)
    val source1 = Source((0 until 30).map(start.plusDays(_)))
    val source2 = Source((0 to 20 by 5).map(start.plusDays(_)))

    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(new BalancedZip[LocalDateTime]((main, enrich) => main.isAfter(enrich) || main.isEqual(enrich)))

      source1 ~> zip.in0
      source2 ~> zip.in1
      zip.out ~> Sink.foreach(println)

      ClosedShape
    }).run()
  }

}
