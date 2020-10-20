package io.monster.ecomm.account.test.environment

import io.monster.ecomm.account.configuration.Configuration
import io.monster.ecomm.account.db.transactor.DbTransactor
import io.monster.ecomm.account.environment.{ AppEnvironment, HttpServerEnvironment }
import io.monster.ecomm.account.repository.{ AccountRepository, UserRepository }
import zio.ULayer
import zio.clock.Clock

case class ItTestEnvironment(host: String, port: Int) {
  private val config = Configuration.testWithDb(host, port)
  val httpServerEnvironmentLayer: ULayer[HttpServerEnvironment] = config ++ Clock.live
  val dbTransactorLayer: ULayer[DbTransactor] = config >>> DbTransactor.live
  val usersRepositoryLayer: ULayer[UserRepository] = dbTransactorLayer >>> UserRepository.live
  val accountRepositoryLayer: ULayer[AccountRepository] = dbTransactorLayer >>> AccountRepository.live
  val appEnvironmentLayer: ULayer[AppEnvironment] =
    httpServerEnvironmentLayer ++ usersRepositoryLayer ++ accountRepositoryLayer
}
