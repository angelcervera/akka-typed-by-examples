package example.server

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import example.api.CounterServiceHandler
import example.server.Main.config

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class CounterServer(actor: ActorRef[CounterActor.Command])(
  implicit system: ActorSystem[_]
) {
  import akka.actor.typed.scaladsl.adapter._
  import system.executionContext
  implicit val scheduler = system.scheduler

  def start() = {

    system.log.info(s"Starting server to redirect calls to ${actor}")

    val counterServiceHandler =
      CounterServiceHandler.partial(
        new CounterServiceImpl(
          actor,
          config.getString("server.incs-implementation")
        )
      )
    // val anotherServiceHandler = ....

    val serviceHandlers: HttpRequest => Future[HttpResponse] =
      ServiceHandler.concatOrNotFound(
        counterServiceHandler
        /*, anotherServiceHandler*/
      )

    val serverBinding =
      Http()(system = system.toClassic).bindAndHandleAsync(
        serviceHandlers,
        interface = config.getString("server.interface"),
        port = config.getInt("server.port"),
        connectionContext = HttpConnectionContext()
      )

    serverBinding.onComplete {
      case Success(bound) =>
        println(
          s"Counter server typed online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
        )
      case Failure(e) =>
        Console.err.println(s"Counter server typed can not start!")
        e.printStackTrace()
        system.terminate()
    }

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
