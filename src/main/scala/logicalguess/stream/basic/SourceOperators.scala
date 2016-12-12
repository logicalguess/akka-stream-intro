package logicalguess.stream.basic

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}

import scala.io.StdIn

/**
  * Created by logicalguess on 12/11/16.
  */
object SourceOperators {

  def isPrime(n: Int): Boolean = {
    if (n <= 1) false
    else if (n == 2) true
    else !(2 to Math.sqrt(n).toInt).exists(x => n % x == 0)
  }

  val source = Source(1 to 100)
  val take = source.take(10)
  val takeWhile = source.takeWhile(_ % 7 != 0)
  val filter = source.filter(isPrime)
  val map = take.map(_ * 2)
  val scan = take.scan(0)(_ + _)
  val fold = take.fold(0)(_ + _)
  val reduce = take.reduce(_ + _)

  val flow = Flow[Int].take(25).filter(isPrime)
  val via = source.via(flow)

  def main(args: Array[String]): Unit = {

    val sources: List[Source[_, _]] = List(source, take, takeWhile, filter, map, scan, fold, reduce, via)
    val names = List("source", "take", "takeWhile", "filter", "map", "scan", "fold", "reduce", "via")

    println("")
    println("***************************")
    println("Choose a Source operator:")
    println("***************************")
    println("")
    Iterator.range(0, sources.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < sources.size) => {
        val source = sources(idx)

        implicit val system = ActorSystem("akka-stream-intro")
        implicit val materializer = ActorMaterializer()

        source.runForeach(println)

        StdIn.readLine()
        system.terminate()
      }
      case _ =>
    }
  }

}
