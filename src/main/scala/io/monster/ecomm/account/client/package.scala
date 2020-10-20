package io.monster.ecomm.account

import io.monster.ecomm.account.client.account.AccountClient
import io.monster.ecomm.account.model.Account
import zio.{ Has, RIO }

package object client {
  type HasAccountClient = Has[AccountClient.AccountClient]

  def get(id: String): RIO[HasAccountClient, Account] = RIO.accessM(_.get.get(id))

  def getAll: RIO[HasAccountClient, List[Account]] = RIO.accessM(_.get.getAll)

  def create(account: Account): RIO[HasAccountClient, Account] = RIO.accessM(_.get.create(account))

  def delete(id: String): RIO[HasAccountClient, Boolean] = RIO.accessM(_.get.delete(id))

  def update(account: Account): RIO[HasAccountClient, Boolean] = RIO.accessM(_.get.update(account))
}
