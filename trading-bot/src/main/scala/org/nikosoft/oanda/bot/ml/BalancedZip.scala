package org.nikosoft.oanda.bot.ml

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class BalancedZip[T](nextEnrichCondition: (T, T) => Boolean) extends GraphStage[FanInShape2[T, T, (T, T)]] {

  private val in0 = Inlet[T]("BalancedZip.in0")
  private val in1 = Inlet[T]("BalancedZip.in1")
  private val out = Outlet[(T, T)]("BalancedZip.out")

  override def shape: FanInShape2[T, T, (T, T)] = new FanInShape2[T, T, (T, T)](in0, in1, out)

  def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var lastEnrichOption: Option[T] = None
    var nextEnrichOption: Option[T] = None
    var lastIncomingOption: Option[T] = None

    var enrichFinished = false

    setHandler(in0, new InHandler {
      def onPush(): Unit = {
        val lastIncoming = grab(in0)
        lastIncomingOption = Option(lastIncoming)

        val enrichOption: Option[T] = (lastEnrichOption, nextEnrichOption) match {
          case (Some(_lastEnrich), Some(nextEnrich)) if nextEnrichCondition(lastIncoming, nextEnrich) =>
            lastEnrichOption = nextEnrichOption
            nextEnrichOption = None
            Option(nextEnrich)
          case (Some(_lastEnrich), _) => Option(_lastEnrich)
          case (None, _) => None
        }
        enrichOption.fold(println("nothing to push"))(enrich => {
          val elem = (lastIncoming, enrich)
          push(out, elem)
        })
      }
    })

    setHandler(in1, new InHandler {
      def onPush(): Unit = {
        val grabbedValue = Option(grab(in1))
        (lastEnrichOption, nextEnrichOption) match {
          case (Some(_), Some(_)) =>
            lastEnrichOption = nextEnrichOption
            nextEnrichOption = grabbedValue
          case (Some(_), None) => nextEnrichOption = grabbedValue
          case (None, None) => lastEnrichOption = grabbedValue
        }
      }

      @throws[Exception](classOf[Exception])
      override def onUpstreamFinish(): Unit = {}
    })

    setHandler(out, new OutHandler {
      def onPull(): Unit = {
        if (nextEnrichOption.isEmpty && !hasBeenPulled(in1)) tryPull(in1)
        pull(in0)
      }
    })
  }
}

object Main extends App {

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
