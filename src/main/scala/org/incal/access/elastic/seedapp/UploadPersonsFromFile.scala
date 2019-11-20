package org.incal.access.elastic.seedapp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.typesafe.scalalogging.Logger
import org.incal.access.elastic.seedapp.model.{AkkaFileUtil, Gender, Person}
import org.incal.access.elastic.seedapp.repo.RepoTypes.PersonRepo
import org.incal.access.elastic.seedapp.repo.{RepoModule, StreamSpec}
import net.codingwell.scalaguice.InjectorExtensions._
import org.incal.access.elastic.seedapp.repo.CrudRepoExtra._
import org.slf4j.LoggerFactory

object UploadPersonsFromFile extends App {

  // file to upload persons from
  private val inputFileName = getClass.getClassLoader.getResource("persons_upload.csv").getFile()

  // logger
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val execContext = system.dispatcher

  // create an application injector and get the person repo
  private val injector = Guice.createInjector(new ConfigModule(), new RepoModule())
  private val personRepo = injector.instance[PersonRepo]

  // upload persons from a given file name
  uploadPersonsFromFile(inputFileName).onComplete { _ =>
    logger.info("Persons successfully uploaded. Terminating...")
    system.terminate().onComplete { _ => System.exit(1) }
  }

  private def uploadPersonsFromFile(csvFileName: String) = {
    logger.info(s"Loading persons from the file '${csvFileName}'.")

    // parse the csv and create a stream of persons
    val personInputStream = AkkaFileUtil.csvAsSourceWithTransform(csvFileName,
      header => {
        val columnIndexMap = header.map(_.trim).zipWithIndex.toMap
        val nameColumnIndex = columnIndexMap.get("name").get
        val ageColumnIndex = columnIndexMap.get("age").get
        val genderColumnIndex = columnIndexMap.get("gender").get
        val diedColumnIndex = columnIndexMap.get("died").get

        els =>
          Person(
            name = els(nameColumnIndex).trim,
            age = els(ageColumnIndex).trim.toInt,
            gender = Gender.withName(els(genderColumnIndex).trim),
            died = els(diedColumnIndex).trim.toBoolean
          )
      }
    )

    // default stream spec... if needed batch size, backpressure size and parallelism can be specified
    val streamSpec = StreamSpec()

    // save the stream
    personRepo.saveAsStream(personInputStream, streamSpec)
  }
}
