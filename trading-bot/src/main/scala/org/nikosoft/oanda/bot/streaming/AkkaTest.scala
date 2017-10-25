package org.nikosoft.oanda.bot.streaming

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}
import akka.util.ByteString

object AkkaTest extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val slowFlow = Flow[String].map(i => {
    Thread.sleep(1000)
    i
  })

  def buildFlow(parallelism: Int) = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val dispatcher = builder.add(Balance[String](parallelism))
    val merge = builder.add(Merge[String](parallelism))

    (0 until parallelism).foreach { i =>
      dispatcher.out(i) ~> slowFlow.async ~> merge.in(i)
    }

    FlowShape(dispatcher.in, merge.out)
  })

  FileIO.fromPath(Paths.get("/Users/niko/projects/oanda-trader/eur_usd_train_2017_M5.csv"))
    .via(Framing.delimiter(ByteString("\n"), Int.MaxValue, allowTruncation = false).map(_.utf8String))
    .via(buildFlow(6))
    .runWith(Sink.foreach(println))

}
