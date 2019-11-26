package org.incal.access.elastic.seedapp.repo

import java.util.{Date, UUID}

import com.sksamuel.elastic4s.http.HttpClient
import javax.inject.Inject

import org.incal.access.elastic.ElasticSetting
import org.incal.access.elastic.caseclass.{ElasticCaseClassAsyncCrudRepo, TypeValueConverters}
import org.incal.access.elastic.seedapp.model.{Gender, Person}

private class ElasticPersonRepo @Inject()(
  val client: HttpClient
) extends ElasticCaseClassAsyncCrudRepo[Person, UUID](
  "persons", "persons", ElasticSetting()
) with RepoTypes.PersonRepo {

  createIndexIfNeeded

  private val includeInAll = false

  // schema / definitions of the fields
  override protected def fieldDefs = Seq(
    keywordField("id") store true includeInAll(includeInAll),
    textField("name") store true includeInAll(includeInAll),
    longField("age") store true includeInAll(includeInAll),
    keywordField("gender") store true includeInAll(includeInAll),
    booleanField("died") store true includeInAll(includeInAll),
    dateField("timeCreated") store true includeInAll(includeInAll)
  )

  // note that the mappings could be simplified if instead of the case-class reflection-based serializer we use a JSON format: i.e. a Spray or Play serializer
  override protected def valueToJsonString(value: Any): Option[String] =
    value match {
      case gender: Gender.Value => super.valueToJsonString(gender.toString)
      case _ => super.valueToJsonString(value)
    }

  override protected val typeValueConverters =
    Seq(TypeValueConverters.date, TypeValueConverters.uuid, TypeValueConverters.enum(Gender))
}