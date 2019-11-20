package org.incal.access.elastic.seedapp

class SeedAppException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this(message: String) = this(message, null)
}

case class SeedAppValidationException(message: String, cause: Throwable) extends SeedAppException(message, cause) {
  def this(message: String) = this(message, null)
}