package example.server

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, Scheduler}
import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import example.api
import example.api.{Increment, State}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

// TODO: Check https://doc.akka.io/docs/akka/2.6/typed/from-classic.html#ask and https://doc.akka.io/docs/akka/2.6/typed/from-classic.html#pipeto
class CounterServiceImpl(counter: ActorRef[CounterActor.Command])(
  implicit
  executionContext: ExecutionContext,
  scheduler: Scheduler
) extends api.CounterService {

  def stateAdapter(state: CounterActor.State): api.State =
    api.State(state.events, state.acc)

  // FIXME: Temporal timeout for the example
  implicit val timeout = Timeout(1.minutes)

  override def inc(in: api.Increment): Future[api.State] =
    counter
      .ask[CounterActor.State](replyTo => CounterActor.Increment(in.v, replyTo))
      .map(stateAdapter)

  override def get(in: api.Empty): Future[api.State] =
    counter
      .ask[CounterActor.State](replyTo => CounterActor.GetState(replyTo))
      .map(stateAdapter)

  override def incs(
    in: Source[api.Increment, NotUsed]
  ): Source[api.State, NotUsed] =
    in.via(
        ActorFlow.ask(counter)(
          (inc, replyTo: ActorRef[CounterActor.State]) =>
            CounterActor.Increment(inc.v, replyTo)
        )
      )
      .map(stateAdapter)

  override def incsOneResponse(in: Source[Increment, NotUsed]): Future[State] =
    ???
}
