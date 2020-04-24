package example.server

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorTags, Behavior}

object Guardian {
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      implicit val system = context.system
      implicit val config = system.settings.config
      val counter = CounterActor(context)
      new CounterServer(counter).start();
      Behaviors.empty
    }
  }
}
