package io.monster.ecomm.account.db

import zio.Has

package object transactor {
  type DbTransactor = Has[DbTransactor.Resource]
}
