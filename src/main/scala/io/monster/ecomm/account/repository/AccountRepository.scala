package io.monster.ecomm.account.repository

import io.monster.ecomm.account.db.transactor.DbTransactor
import io.monster.ecomm.account.model.Account
import zio.{ Task, URLayer, ZLayer }

object AccountRepository {
  trait AccountService {
    def getAccount(id: String): Task[Account]

    def getAllAccounts: Task[List[Account]]

    def createAccount(account: Account): Task[Account]

    def deleteAccount(id: String): Task[Boolean]

    def updateAccount(account: Account): Task[Boolean]
  }

  val live: URLayer[DbTransactor, AccountRepository] =
    ZLayer.fromService[DbTransactor.Resource, AccountService] { dbTransactor =>
      AccountRepositoryImpl(dbTransactor.xa)
    }
}
