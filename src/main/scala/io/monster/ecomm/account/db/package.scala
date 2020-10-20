package io.monster.ecomm.account

import io.monster.ecomm.account.configuration.Configuration.DbConfig
import zio.Has

package object db {
  type DBConfig = Has[DbConfig]
}
