package logicalguess.stream.actor

import akka.stream.actor.ActorPublisher
import logicalguess.stream.domain.Event

/**
  * Created by logicalguess on 12/11/16.
  */

case object END

class SourceActor extends ActorPublisher[Event] {
  def receive: PartialFunction[Any, Unit] = {
    case event: Event => onNext(event)
    case END => onComplete()
  }
}
