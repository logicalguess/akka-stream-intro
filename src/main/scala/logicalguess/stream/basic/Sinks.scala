package logicalguess.stream.basic

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Balance, Keep, RunnableGraph, Sink, Source}
import logicalguess.stream.actor.SinkActor

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
  * Created by logicalguess on 12/11/16.
  */
object Sinks {

  val ignore: Sink[Int, Future[Done]] = Sink.ignore
  val foreach: Sink[Int, Future[Done]] = Sink.foreach(println)
  val fold: Sink[Int, Future[Int]] = Sink.fold(0) {_ + _}
  val reduce: Sink[Int, Future[Int]] = Sink.reduce {_ + _}

  val combine: Sink[Int, _] = Sink.combine(foreach, fold)(Balance[Int](_)) //Broadcast[_]

  val actorSink: Sink[Int, ActorRef] = Sink.actorSubscriber(Props(classOf[SinkActor], "sinkActor"))


  def main(args: Array[String]): Unit = {

    val sinks = List(ignore, foreach, fold, reduce, combine, actorSink)
    val names = List("ignore", "foreach", "fold", "reduce", "combine", "actor")

    println("")
    println("***************************")
    println("Choose a Sink:")
    println("***************************")
    println("")
    Iterator.range(0, sinks.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < sinks.size) => {
        val sink = sinks(idx)


        import system.dispatcher

        implicit val system = ActorSystem("akka-stream-intro")
        implicit val materializer = ActorMaterializer()

        val graph: RunnableGraph[_] = Source(1 to 10).toMat(sink)(Keep.right)
        val result = graph.run()

        result match {
          case result: Future[_] => {

            result.onComplete {
              r => {
                r match {
                  case Success(s) => println(s)
                  case Failure(e) => println("Error! " + e.getMessage)
                }

                val whenTerminated = system.terminate()
                Await.result(whenTerminated, Duration.Inf)
              }
            }
          }
          case r => {
            println(r)

            StdIn.readLine()

            val whenTerminated = system.terminate()
            Await.result(whenTerminated, Duration.Inf)
          }
        }
      }
      case _ =>
    }
  }
}
