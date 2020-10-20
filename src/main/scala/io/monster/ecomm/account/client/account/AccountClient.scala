package io.monster.ecomm.account.client.account

import io.monster.ecomm.account.client.HasAccountClient
import io.monster.ecomm.account.configuration.Configuration.AccountClientConfig
import io.monster.ecomm.account.model.Account
import org.http4s.client.Client
import zio._

object AccountClient {

  trait AccountClient {
    def get(id: String): Task[Account]

    def getAll: Task[List[Account]]

    def create(account: Account): Task[Account]

    def delete(id: String): Task[Boolean]

    def update(account: Account): Task[Boolean]
  }

  val accountClientLiveLayer
    : URLayer[Has[AccountClientConfig] with Has[TaskManaged[Client[Task]]], HasAccountClient] =
    ZLayer.fromServices[TaskManaged[Client[Task]], AccountClientConfig, AccountClient] {
      (client, accountClientConfig) =>
        new AccountClientImpl(client, accountClientConfig)
    }
}
