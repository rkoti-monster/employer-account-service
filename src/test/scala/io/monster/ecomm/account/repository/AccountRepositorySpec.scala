package io.monster.ecomm.account.repository

import io.monster.ecomm.account.environment.TestEnvironments.accountRepositoryLayer
import io.monster.ecomm.account.model.{ Account, AccountNotFound }
import zio.test.Assertion._
import zio.test.TestAspect.sequential
import zio.test._
import zio.test.environment.TestEnvironment

object AccountRepositorySpec extends DefaultRunnableSpec {
  val account1 = Account("13", "usr", None, None, None, None, None, None)
  val account2 = Account("13", "newUsr", None, None, None, None, None, None)
  val account3 = Account("14", "usr2", None, None, None, None, None, None)
  val missingAccount = Account("1", "missingAccount", None, None, None, None, None, None)

  def spec =
    suite("Account Persistence test")(testM("Account Repository test") {
      for {
        notFound       <- getAccount("100").either
        created        <- createAccount(account1).either
        updated        <- updateAccount(account2).either
        found          <- getAccount(account1.id).either
        recreated      <- createAccount(account1).either
        createdAnother <- createAccount(account3).either
        foundAll       <- getAllAccounts.either
        updateFailed   <- updateAccount(missingAccount).either
        deleted        <- deleteAccount(account1.id).either
        deleteFailed   <- deleteAccount(account1.id).either
        allDeleted     <- deleteAccount(account3.id).either
        empty          <- getAllAccounts.either
      } yield assert(notFound)(isLeft(anything)) &&
        assert(created)(isRight(equalTo(account1))) &&
        assert(updated)(isRight(equalTo(true))) &&
        assert(found)(isRight(equalTo(account2))) &&
        assert(recreated)(isRight(equalTo(account2))) &&
        assert(createdAnother)(isRight(equalTo(account3))) &&
        assert(foundAll)(isRight(equalTo(List(account2, account3)))) &&
        assert(updateFailed)(isLeft(equalTo(AccountNotFound(missingAccount.id)))) &&
        assert(deleted)(isRight(isTrue)) &&
        assert(deleteFailed)(isLeft(equalTo(AccountNotFound(account1.id)))) &&
        assert(allDeleted)(isRight(isTrue)) &&
        assert(empty)(isRight(equalTo(List())))
    }).provideSomeLayer[TestEnvironment](accountRepositoryLayer) @@ sequential
}
