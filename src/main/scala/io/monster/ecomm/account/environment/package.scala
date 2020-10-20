package io.monster.ecomm.account

import io.monster.ecomm.account.configuration.Configuration
import io.monster.ecomm.account.repository.{ AccountRepository, UserRepository }
import zio.clock.Clock

package object environment {
  type HttpServerEnvironment = Configuration with Clock
  type AppEnvironment = HttpServerEnvironment with UserRepository with AccountRepository
}
