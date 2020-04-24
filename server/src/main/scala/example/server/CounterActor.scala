package example.server

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ActorRef, ActorTags, Behavior}

object CounterActor {

  trait Protocol
  trait Command extends Protocol
  trait Reply extends Protocol

  final case class State(events: Long, acc: Long) extends Reply

  final case class Increment(v: Long, ref: ActorRef[State]) extends Command
  final case class GetState(replyTo: ActorRef[State]) extends Command

  def apply(context: ActorContext[Nothing]): ActorRef[Command] = {
    val config = context.system.settings.config
    val persist = config.getBoolean("server.persist")
    val delayNanos = config.getDuration("server.event-delay").toNanos
    if (persist) {
      context.log.info("Enabling Persistence/ES Actor")
      context.spawn(
        CounterPersistenceActor("CounterActorId", delayNanos),
        "counter",
        ActorTags("counter-bc")
      )
    } else {
      context.log.info("Enabling plain Actor")
      context.spawn(
        CounterNoPersistenceActor(State(0, 0), delayNanos),
        "counter",
        ActorTags("counter-bc")
      )
    }
  }
}
