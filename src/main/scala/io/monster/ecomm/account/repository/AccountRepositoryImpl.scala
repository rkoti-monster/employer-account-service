package io.monster.ecomm.account.repository

import com.github.mlangc.slf4zio.api.{ LoggingSupport, Slf4jLoggerOps }
import doobie.implicits._
import doobie.{ ConnectionIO, Transactor }
import io.getquill.{ idiom => _ }
import io.monster.ecomm.account.model.schema._
import io.monster.ecomm.account.model.{ Account, AccountNotFound }
import io.monster.ecomm.account.repository.AccountRepository.AccountService
import zio.Task
import zio.interop.catz._

final case class AccountRepositoryImpl(xa: Transactor[Task]) extends AccountService with LoggingSupport {
  def getAccount(id: String): Task[Account] =
    logger.infoIO(s"Fetching account for id: $id") *>
      SQL
        .getAccount(id)
        .transact(xa)
        .foldM(
          err => Task.fail(err),
          {
            case Nil          => Task.fail(AccountNotFound(id))
            case account :: _ => Task.succeed(account)
          }
        )

  def getAllAccounts: Task[List[Account]] =
    SQL.getAllAccounts.transact(xa).foldM(err => Task.fail(err), output => Task.succeed(output))

  def createAccount(account: Account): Task[Account] =
    getAccount(account.id).foldM(
      _ => SQL.createAccount(account).transact(xa).foldM(err => Task.fail(err), _ => Task.succeed(account)),
      existingAccount =>
        logger.infoIO(
          s"$account already exists. Please update the account if attributes are to be changed."
        ) *> Task.succeed(existingAccount)
    )

  def deleteAccount(id: String): Task[Boolean] =
    getAccount(id).foldM(
      _ => Task.fail(AccountNotFound(id)),
      account =>
        logger.infoIO(s"Deleting ${account}")
          *> SQL.deleteAccount(id).transact(xa).fold(_ => false, _ => true)
    )

  def updateAccount(account: Account): Task[Boolean] =
    getAccount(account.id).foldM(
      _ => logger.infoIO(s"Account not found: ${account}") *> Task.fail(AccountNotFound(account.id)),
      existingAccount =>
        logger.infoIO(s"Updating user ${existingAccount} to ${account}")
          *> SQL.updateAccount(account).transact(xa).fold(_ => false, _ => true)
    )

  object SQL {

    import dc._

    def getAllAccounts: ConnectionIO[List[Account]] = run(quote(account))

    def getAccount(id: String): ConnectionIO[List[Account]] = run(quote(account.filter(_.id == lift(id))))

    def createAccount(acc: Account): doobie.ConnectionIO[_] = run(quote(account.insert(lift(acc))))

    def deleteAccount(id: String): ConnectionIO[_] = run(quote(account.filter(_.id == lift(id)).delete))

    def updateAccount(acc: Account): ConnectionIO[_] =
      run(quote(account.filter(_.id == lift(acc.id)).update(lift(acc))))
  }

}
