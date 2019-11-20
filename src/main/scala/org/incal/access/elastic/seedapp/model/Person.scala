package org.incal.access.elastic.seedapp.model

import java.util.{Date, UUID}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.incal.access.elastic.seedapp.model.Formats._
import org.incal.core.Identity
import spray.json.DefaultJsonProtocol

final case class Person(
  id: Option[UUID] = None,
  name: String,
  age: Int,
  gender: Gender.Value,
  died: Boolean,
  timeCreated: Date = new Date()
)

object Gender extends Enumeration {
  val Male, Female = Value
}

object Person extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object PersonIdentity extends Identity[Person, UUID] {
    override def name = "id"
    override def of(entity: Person) = entity.id
    override def next = UUID.randomUUID()
    override protected def set(entity: Person, id: Option[UUID]) = entity.copy(id = id)
  }

  implicit val genderFormat = enumFormat(Gender)
  implicit val personFormat = jsonFormat6(Person.apply)
}