package io.monster.ecomm.account.environment

import io.monster.ecomm.account.configuration.Configuration
import io.monster.ecomm.account.db.transactor.DbTransactor
import io.monster.ecomm.account.repository.{ AccountRepository, UserRepository }
import zio.ULayer
import zio.clock.Clock

object Environments {
  val httpServerEnvironmentLayer: ULayer[HttpServerEnvironment] = Configuration.live ++ Clock.live
  val dbTransactorLayer: ULayer[DbTransactor] = Configuration.live >>> DbTransactor.live
  val usersRepositoryLayer: ULayer[UserRepository] = dbTransactorLayer >>> UserRepository.live
  val accountRepositoryLayer: ULayer[AccountRepository] = dbTransactorLayer >>> AccountRepository.live
  val appEnvironmentLayer: ULayer[AppEnvironment] =
    httpServerEnvironmentLayer ++ usersRepositoryLayer ++ accountRepositoryLayer
}
