package io.monster.ecomm.account.environment

import io.monster.ecomm.account.client.HasAccountClient
import io.monster.ecomm.account.client.account.AccountClient.accountClientLiveLayer
import io.monster.ecomm.account.client.http.HttpClient.httpClientLive
import io.monster.ecomm.account.configuration.Configuration
import io.monster.ecomm.account.db.transactor.DbTransactor
import io.monster.ecomm.account.repository.{ AccountRepository, UserRepository }
import zio.ULayer
import zio.clock.Clock

object TestEnvironments {
  val httpServerEnvironmentLayer: ULayer[HttpServerEnvironment] = Configuration.test ++ Clock.live
  val dbTransactorLayer: ULayer[DbTransactor] = Configuration.test >>> DbTransactor.live
  val usersRepositoryLayer: ULayer[UserRepository] = dbTransactorLayer >>> UserRepository.live
  val accountRepositoryLayer: ULayer[AccountRepository] = dbTransactorLayer >>> AccountRepository.live
  val appEnvironmentLayer: ULayer[AppEnvironment] =
    httpServerEnvironmentLayer ++ usersRepositoryLayer ++ accountRepositoryLayer
  val accountClientLayer: ULayer[HasAccountClient] =
    (Configuration.test ++ httpClientLive) >>> accountClientLiveLayer
}
