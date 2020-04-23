package example

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.typesafe.config.Config

object CounterActor {

  trait Protocol
  trait Command extends Protocol
  trait Reply extends Protocol
  trait Event extends Protocol

  final case class State(events: Long, acc: Long) extends Reply

  final case class Increment(v: Long, ref: ActorRef[State]) extends Command
  final case class GetState(replyTo: ActorRef[State]) extends Command

  final case class Incremented(v: Long) extends Event

  def apply(counterId: String)(implicit config: Config): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(counterId),
      emptyState = State(0, 0),
      commandHandler = (state, command) => onCommand(state, command),
      eventHandler = (state, event) => applyEvent(state, event)
    )

  private def onCommand(state: State, cmd: Command)(
    implicit config: Config
  ): Effect[Event, State] =
    cmd match {
      case GetState(replyTo) =>
        replyTo ! state
        Effect.none

      case Increment(v, replyTo) =>
        Effect.persist(Incremented(v)).thenRun { state =>
          Thread.sleep(config.getDuration("server.event-delay").toMillis) // Expensive process time simulation.
          replyTo ! state
        }
    }

  private def applyEvent(state: State, event: Event): State = event match {
    case Incremented(v) =>
      State(state.events + 1, state.acc + v)
  }
}
