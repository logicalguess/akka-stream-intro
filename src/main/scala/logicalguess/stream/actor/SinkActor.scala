package logicalguess.stream.actor

import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnNext}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import com.typesafe.scalalogging.LazyLogging

class SinkActor(name: String, delay: Long) extends ActorSubscriber with LazyLogging {
  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  val actorName = name

  def this(name: String) {
    this(name, 0)
  }

  override def receive: Receive = {
    case OnNext(msg) =>
      println(msg)
      logger.debug(s"Message in actor sink ${self.path} '$actorName': $msg")
    case OnComplete =>
      logger.debug(s"Completed Messgae received in ${self.path} '$actorName'")
    case msg =>
      logger.debug(s"Unknown message $msg in $actorName: ")
  }
}