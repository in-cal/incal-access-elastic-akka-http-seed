package org.incal.access.elastic.seedapp

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{EntityStreamException, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, onSuccess, path, pathPrefix, post, _}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.Materializer
import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import javax.inject.{Inject, Singleton}
import org.incal.access.elastic.seedapp.model.Person
import org.incal.access.elastic.seedapp.model.Person._
import org.incal.access.elastic.seedapp.repo.RepoTypes.PersonRepo
import org.incal.core.dataaccess.Criterion._
import org.slf4j.LoggerFactory
import spray.json._

@ImplementedBy(classOf[RouteFactoryImpl])
trait RouteFactory {
  def apply(implicit materializer: Materializer): Route
}

@Singleton
class RouteFactoryImpl @Inject() (configuration: Config, personRepo: PersonRepo) extends RouteFactory with SprayJsonSupport with DefaultJsonProtocol {

  protected val logger = Logger(LoggerFactory.getLogger(this.getClass))

  implicit val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: SeedAppValidationException =>
        extractUri { uri =>
          logger.warn(s"Request to $uri ended with a validation error: ${e.message}")
          complete(HttpResponse(StatusCodes.BadRequest, entity = HttpEntity(e.message)))
        }

      case e: SeedAppException =>
        extractUri { uri =>
          logger.error(s"Request to $uri ended with a seed app error: ${e.getMessage}", e)
          complete(HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity(e.getMessage)))
        }

      case e: EntityStreamException =>
        extractUri { uri =>
          logger.error(s"Request to $uri ended with an input reading error: ${e.getMessage}")
          complete(HttpResponse(StatusCodes.BadRequest, entity = HttpEntity(s"Reading of input failed due to ${e.getMessage}.")))
        }

      case e: Throwable =>
        extractUri { uri =>
          logger.error(s"Request to $uri ended with a general error: ${e.getMessage}", e)
          complete(HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity(e.getMessage)))
        }
    }

  override def apply(implicit materializer: Materializer): Route =
    handleExceptions(exceptionHandler) {
      get {
        pathPrefix("get" / JavaUUID) { id =>
          onSuccess(personRepo.get(id)) {
            case Some(person) => complete(person)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      get {
        pathPrefix("search" / Remaining) { name =>
          onSuccess(personRepo.find(Seq("name" #~ s".*${name.toLowerCase()}.*"))) { ids =>
            complete(ids.toSeq)
          }
        }
      } ~
      get {
        pathPrefix("count") {
          onSuccess(personRepo.count()) { count =>
            complete(count.toString)
          }
        }
      } ~
      post {
        path("add") {
          entity(as[Person]) { person =>
            onSuccess(personRepo.save(person)) { id =>
              complete(StatusCodes.Created, id.toString)
            }
          }
        }
      } ~
      post {
        path("clear") {
          onSuccess(personRepo.deleteAll) {
            complete(s"All persons deleted.")
          }
        }
      }
    }
}