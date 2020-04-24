package example.server

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import example.server.CounterActor._

object CounterPersistenceActor {

  sealed trait Event
  final case class Incremented(v: Long) extends Event

  def apply(counterId: String, delayNanos: Long): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(counterId),
      emptyState = State(0, 0),
      commandHandler = (state, command) => onCommand(state, command, delayNanos),
      eventHandler = (state, event) => applyEvent(state, event)
    )

  private def onCommand(state: State,
                        cmd: Command,
                        delayNanos: Long): Effect[Event, State] =
    cmd match {
      case GetState(replyTo) =>
        replyTo ! state
        Effect.none

      case Increment(v, replyTo) =>
        Effect.persist(Incremented(v)).thenRun { state =>
          pi(delayNanos) // Expensive process time simulation.
          replyTo ! state
        }
    }

  private def applyEvent(state: State, event: Event): State = event match {
    case Incremented(v) =>
      State(state.events + 1, state.acc + v)
  }
}
