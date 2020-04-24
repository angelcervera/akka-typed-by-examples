package example.server

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import example.server.CounterActor._

object CounterNoPersistenceActor {

  def apply(state: State, delayNanos: Long): Behavior[Command] =
    Behaviors.receiveMessage {
      case Increment(v, replyTo) =>
        pi(delayNanos) // Expensive process time simulation.
        val newState = State(state.events + 1, state.acc + v)
        replyTo ! newState
        CounterNoPersistenceActor(newState, delayNanos)
      case GetState(replyTo) =>
        replyTo ! state
        Behaviors.same
    }

}
