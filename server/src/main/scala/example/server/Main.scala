package example.server

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

object Main extends App {

  val config = ConfigFactory
    .parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.load())

  ActorSystem[Nothing](Guardian(), "CounterExample", config)

}
