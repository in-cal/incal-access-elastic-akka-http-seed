package org.incal.access.elastic.seedapp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import net.codingwell.scalaguice.InjectorExtensions._
import org.incal.access.elastic.seedapp.repo.RepoModule
import org.slf4j.LoggerFactory

import scala.io.StdIn

object SeedAppServer extends App {

  // logger
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // obtain the route(s) through the application injector
  private val injector = Guice.createInjector(new ConfigModule(), new RepoModule())
  private val config = injector.instance[Config]
  private val routeFactory = injector.instance[RouteFactory]
  private val route = routeFactory.apply

  // bind to the port
  private val host = config.getString("seed_app.host")
  private val port = config.getInt("seed_app.port")
  private val bindingFuture = Http().bindAndHandle(route, host, port)

  logger.info(s"Seed-app server online at http://$host:$port/\nPress RETURN to stop...")

  StdIn.readLine() // let it run until user presses return

  // needed for the future flatmap at the end
  implicit val executionContext = system.dispatcher

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
}