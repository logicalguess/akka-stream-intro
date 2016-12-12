package logicalguess.stream.backpressure

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import logicalguess.stream.actor.SlowSinkActor
import scala.concurrent.duration._

import scala.io.StdIn

/**
  * Created by logicalguess on 12/11/16.
  */
object Buffer {


  val ticks = Source.tick(initialDelay = 0 millis, interval = 10 millis, "Tick")
  val ints = Source.fromIterator(() => Iterator from 0).map(_.toString)

    val normal = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>

      val source = builder.add(Source.zipWithN[String, String](s => s(1))(List(ticks, ints)))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowSinkActor], "slowSink", 100l)))

      import GraphDSL.Implicits._

      source ~> slowingSink

      ClosedShape
    })

  val fail = fastPublisherSlowingSubscriberWithBuffer(OverflowStrategy.fail)
  val dropHead = fastPublisherSlowingSubscriberWithBuffer(OverflowStrategy.dropHead)
  val backPressure = fastPublisherSlowingSubscriberWithBuffer(OverflowStrategy.backpressure)


  def fastPublisherSlowingSubscriberWithBuffer(strategy: OverflowStrategy) = {

    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>

      val source = builder.add(Source.zipWithN[String, String](s => s(1))(List(ticks, ints)))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowSinkActor], "slowSink", 100l)))

      // create a buffer, with 100 messages, with an overflow
      // strategy that starts dropping the oldest messages when it is getting
      // too far behind.
      val bufferFlow = Flow[String].buffer(100, strategy)

      import GraphDSL.Implicits._

      source ~> bufferFlow ~> slowingSink

      ClosedShape
    })
  }

  def main(args: Array[String]): Unit = {

    val graphs = List(normal, fail, dropHead, backPressure)
    val names = List("Normal", "Fail", "Drop Head", "Back Pressure")

    println("")
    println("***************************")
    println("Choose a Strategy:")
    println("***************************")
    println("")
    Iterator.range(0, graphs.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < graphs.size) => {
        val g = graphs(idx)

        implicit val system = ActorSystem("akka-stream-intro")
        implicit val materializer = ActorMaterializer()

        g.run()

        StdIn.readLine()
        system.terminate()
      }
      case _ =>
    }
  }

}
