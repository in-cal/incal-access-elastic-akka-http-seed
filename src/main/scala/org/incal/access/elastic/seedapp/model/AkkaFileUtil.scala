package org.incal.access.elastic.seedapp.model

import java.nio.file.Paths

import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString

import scala.concurrent.Future

// TODO: should be moved to incal-core
object AkkaFileUtil {

  def fileSource(
    fileName: String,
    eol: String,
    allowTruncation: Boolean
  ) =
    FileIO.fromPath(Paths.get(fileName))
      .via(Framing.delimiter(ByteString(eol), 1000000, allowTruncation)
        .map(_.utf8String))

  def csvAsSourceWithTransform[T](
    fileName: String,
    withHeaderTrans: Array[String] => Array[String] => T,
    delimiter: String = ",",
    eol: String = "\n",
    allowTruncation: Boolean = true
  ): Source[T, _] = {
    // file source
    val source = fileSource(fileName, eol, allowTruncation)

    // skip the head, split lines, and apply a given transformation
    source.prefixAndTail(1).flatMapConcat { case (first, tail) =>
      val header = first.head.split(delimiter, -1)
      val processEls = withHeaderTrans(header)
      tail.map { line =>
        val els = line.split(delimiter, -1)
        processEls(els)
      }
    }
  }

  def writeLines(
    source: Source[String, _],
    fileName: String,
    eol: String = "\n")(
    implicit materializer: Materializer
  ): Future[IOResult] =
    source
      .map(line => ByteString(line + eol))
      .runWith(FileIO.toPath(Paths.get(fileName)))
}
