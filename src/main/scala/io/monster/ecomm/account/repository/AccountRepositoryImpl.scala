package io.monster.ecomm.account.repository

import doobie.implicits._
import doobie.{LogHandler, Query0, Transactor, Update0}
import io.monster.ecomm.account.model.{Account, AccountNotFound}
import io.monster.ecomm.account.repository.Repository.AccountService
import zio.{Task, ZIO}
import zio.console.Console
import zio.interop.catz._

final case class AccountRepositoryImpl(xa: Transactor[Task], console: Console.Service) extends AccountService {
  def getAccount(id: String): Task[Account] = {
    console.putStrLn(s"Get the account for account id: ${id}") *>
    SQL2.getAccount(id)
      .option
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        maybeAccount => Task.require(AccountNotFound(id))(Task.succeed(maybeAccount)))
  }

  def getAllAccounts: Task[List[Account]] = {
    SQL2.getAllAccounts().to[List].transact(xa).foldM(err => Task.fail(err), output => Task.succeed(output))
  }

  def createAccount(account: Account): Task[Account] = {
    getAccount(account.id).flatMap(
      account => ZIO.effectTotal(println("There is a duplicate, so not inserting")) *> Task.succeed(account)
    ).orElse(
      SQL2.createAccount(account).run.transact(xa).foldM(err => Task.fail(err), _ => Task.succeed(account))
    )
  }

  def deleteAccount(id: String): Task[Boolean] =
    SQL2
      .deleteAccount(id)
      .run
      .transact(xa)
      .fold(_ => false, _ => true)

  def updateAccount(account: Account): Task[Boolean] = {
    getAccount(account.id).flatMap(
      account1 => Task.succeed(println(s"Updating account ${account1} to ${account}")) *>
        SQL2
          .updateAccount(account)
          .run
          .transact(xa)
          .fold(_ => false, _ => true)
    ).orElse(
      Task.fail(AccountNotFound(account.id))
    )
  }
}



object SQL2 {

  def getAllAccounts(): Query0[Account] =
    sql"""SELECT  id, name, contact_id, zuora_id, crm_id, website, parent_account_id, address FROM ACCOUNT""".queryWithLogHandler[Account](LogHandler.jdkLogHandler)

  def getAccount(id: String): Query0[Account] =
    sql"""SELECT  id, name, contact_id, zuora_id, crm_id, website, parent_account_id, address FROM ACCOUNT where id=${id}""".queryWithLogHandler[Account](LogHandler.jdkLogHandler)

  def createAccount(account: Account): Update0 =
    sql"""INSERT INTO ACCOUNT (id, name, contact_id, zuora_id, crm_id, website, parent_account_id, address) VALUES
         (${account.id}, ${account.name}, ${account.contactId}, ${account.zuoraId}, ${account.crmId}, ${account.website}, ${account.parentAccountId}, ${account.address})
         """.updateWithLogHandler(LogHandler.jdkLogHandler)

  def deleteAccount(id: String): Update0 =
    sql"""DELETE FROM ACCOUNT WHERE id = $id""".update

  def updateAccount(account: Account): Update0 =
    sql"""UPDATE ACCOUNT SET name = ${account.name} WHERE id = ${account.id}""".updateWithLogHandler(LogHandler.jdkLogHandler)
}







