package logicalguess.stream.actor

import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import com.typesafe.scalalogging.LazyLogging

/**
 * An actor that progressively slows as it processes messages.
 */
class SlowSinkActor(name: String, delayPerMsg: Long, initialDelay: Long) extends ActorSubscriber with LazyLogging {
  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  var delay = 0l

  def this(name: String) {
    this(name, 0, 0)
  }

  def this(name: String, delayPerMsg: Long) {
    this(name, delayPerMsg, 0)
  }

  override def receive: Receive = {

    case OnNext(msg: String) =>
      delay += delayPerMsg
      Thread.sleep(initialDelay + (delay / 1000))
      println(s"Message: $msg")
      logger.debug(s"Message in slowdown actor sink ${self.path} '$name': $msg")
    case msg =>
      logger.debug(s"Unknown message $msg in $name: ")
  }
}

