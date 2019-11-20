package org.incal.access.elastic.seedapp.model

import java.util.{Date, UUID}

import spray.json.{DeserializationException, JsNumber, JsString, JsValue, JsonFormat}

object Formats {

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue) =
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
  }

  implicit object DateFormat extends JsonFormat[Date] {
    def write(date: Date) = JsNumber(date.getTime)

    def read(value: JsValue) =
      value match {
        case JsNumber(ms) =>
          try {
            new Date(ms.toLongExact)
          } catch {
            case e: java.lang.ArithmeticException => throw new DeserializationException(s"Expected date/time in the milliseconds format but got: ${ms.toString}.")
          }
        case _ =>
          throw new DeserializationException(s"Expected date/time in the milliseconds format but got a non-number: ${value.toString}")
      }
  }

  def enumFormat[E <: Enumeration](enum: E): JsonFormat[E#Value] = new EnumFormat(enum)
}

private class EnumFormat[E <: Enumeration](enum: E) extends JsonFormat[E#Value] {
  def write(gender: E#Value) = JsString(gender.toString)

  def read(value: JsValue) =
    value match {
      case JsString(value) =>
        try {
          enum.withName(value)
        } catch {
          case _: NoSuchElementException => throw new DeserializationException(s"Enumeration of type '${enum.getClass}' does not appear to contain the value '$value'.")
        }
      case _ => throw new DeserializationException(s"Expected String but got ${value}")
    }
}
