package org.incal.access.elastic.seedapp.repo

import java.util.UUID

import com.sksamuel.elastic4s.http.HttpClient
import net.codingwell.scalaguice.ScalaModule
import org.incal.access.elastic.BasicElasticClientProvider
import org.incal.core.dataaccess.AsyncCrudRepo
import org.incal.access.elastic.seedapp.model.Person
import org.incal.access.elastic.seedapp.repo.RepoTypes.PersonRepo

class RepoModule extends ScalaModule {
  override def configure = {
    bind[HttpClient].toProvider(new BasicElasticClientProvider).asEagerSingleton
    bind[PersonRepo].to(classOf[ElasticPersonRepo]).asEagerSingleton
  }
}

object RepoTypes {
  type PersonRepo = AsyncCrudRepo[Person, UUID]
}
