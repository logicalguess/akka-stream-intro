package logicalguess.stream.basic

import java.io.File

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl._
import akka.util.ByteString
import logicalguess.stream.actor.{END, SourceActor}
import logicalguess.stream.domain.{Event, RandomEvent}

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.io.StdIn

object Sources {

  val single = Source.single("The One").named("single")
  val repeat = Source.repeat("Tick Tock")
  val cycle = Source.cycle(() => Iterator.range(1, 6))
  val ticks = Source.tick(initialDelay = 0 millis, interval = 100 millis, "Tick")

  val list = Source(List("Event 1", "Event 2", "Event 3", "Event 4", "Event 5"))
  val range = Source(1 to 10)
  val randoms = Source.fromIterator(() => Iterator.continually(ThreadLocalRandom.current().nextInt(100)))

  val file = FileIO.fromPath(new File("src/main/resources/logfile.txt").toPath)
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 512, allowTruncation = true))
    .map(_.utf8String)

  val combine = Source.combine(repeat.take(10), single)(Concat(_))
  //Concat
  val zip = Source.zipN(List(cycle, ticks))

  val actorSource: Source[Int, ActorRef] = Source.actorPublisher[Int](Props[SourceActor])

  val queueSource: Source[Event, SourceQueueWithComplete[Event]] =
    Source.queue[Event](10, OverflowStrategy.backpressure)
      .throttle(1, Duration(100, "millisecond"), 1, ThrottleMode.shaping)

  private def publishEvents(result: Any) = {
    result match {
      case actor: ActorRef => {
        actor ! RandomEvent()
        actor ! RandomEvent()
        actor ! RandomEvent()
        actor ! END
      }
      case queue: SourceQueueWithComplete[Event] => {
        queue.offer(RandomEvent())
        queue.offer(RandomEvent())
        queue.offer(RandomEvent())
    }
      case _ =>
    }
  }

  private def graph(source: Source[_, _], action: Any => Unit): RunnableGraph[_] = {
    source.to(Sink.foreach(action))
  }

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("akka-stream-intro")
    implicit val materializer = ActorMaterializer()

    val sources = List(single, repeat, cycle, ticks, list, range, randoms, file, combine,
      zip, actorSource, queueSource)
    val names = List("single", "repeat", "cycle", "ticks", "list", "range", "randoms", "file", "combine",
      "zip", "actor", "queue")

    println("")
    println("***************************")
    println("Choose a Source:")
    println("***************************")
    println("")
    Iterator.range(0, sources.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < sources.size) => {
        val source = sources(idx)
        val result = graph(source, println).run()
        publishEvents(result)
      }
      case _ =>
    }

    StdIn.readLine()
    system.terminate()
  }
}

