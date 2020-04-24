package example.server

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class CounterPersistenceActorSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers {

  "CounterActor" should {
    "Start with zero" in {
      val counter =
        testKit.spawn(
          CounterPersistenceActor("test_zero", 2.seconds.toNanos),
          "test_zero"
        )
      val probe = testKit.createTestProbe[CounterActor.State]()
      counter ! CounterActor.GetState(probe.ref)
      probe.expectMessage(CounterActor.State(0, 0))
    }

    "Increment and count events processed" in {
      val counter =
        testKit.spawn(
          CounterPersistenceActor("test_zero", 2.seconds.toNanos),
          "test_inc"
        )
      val probe = testKit.createTestProbe[CounterActor.State]()
      val response = testKit.createTestProbe[CounterActor.State]()
      counter ! CounterActor.Increment(10, response.ref)
      counter ! CounterActor.GetState(probe.ref)
      probe.expectMessage(CounterActor.State(1, 10))
    }
  }

  "Testing the AKKA Persistence" should {
    "recover" in {
      implicit val timeout = Timeout(3.seconds)

      val response = testKit.createTestProbe[CounterActor.State]()

      val counter = testKit.spawn(
        CounterPersistenceActor("testing_recovering", 1.milli.toNanos),
        "testing_initializing"
      )
      1 to 100 foreach (_ => counter ! CounterActor.Increment(10, response.ref))

      val probe = testKit.createTestProbe[CounterActor.State]()
      counter ! CounterActor.GetState(probe.ref)

      probe.expectMessage(CounterActor.State(100, 1000))

      testKit.stop(counter)

      val counterRecovered =
        testKit.spawn(
          CounterPersistenceActor("testing_recovering", 1.milli.toNanos),
          "testing_recovered"
        )
      counterRecovered ! CounterActor.GetState(probe.ref)
      probe.expectMessage(CounterActor.State(100, 1000))
    }

  }

}
