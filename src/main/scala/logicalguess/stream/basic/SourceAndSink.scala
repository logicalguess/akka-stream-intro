package logicalguess.stream.basic

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}

import scala.concurrent.duration._

/**
 * The simplest possible akka stream.
 */
object SourceAndSink {

  def flow(implicit materializer: ActorMaterializer) = {

    val events = "Event 1" :: "Event 2" :: "Event 3" :: "Event 4" :: "Event 5" :: Nil

    Source(events)
      .map(println(_))
      .to(Sink.ignore)
  }

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    flow.run()

    system.terminate()
  }
}
