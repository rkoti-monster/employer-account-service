package io.monster.ecomm.account.repository

import io.monster.ecomm.account.environment.TestEnvironments.accountRepositoryLayer
import io.monster.ecomm.account.model.Account
import zio.test.Assertion.{anything, equalTo, isLeft, isRight, isTrue}
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, assert, suite, testM}

object AccountRepositorySpec extends DefaultRunnableSpec {
  val account1 = Account("13", "usr", None, None, None, None, None, None)
  val account2 = Account("13", "newUsr", None, None, None, None, None, None)
  val account3 = Account("14", "usr2", None, None, None, None, None, None)

  def spec =
    suite("User Persistence test")(testM("Account Repository test") {
      val ec = concurrent.ExecutionContext.global
      for {
        notFound <- getAccount("100").either
        created <- createAccount(account1).either
        updated <- updateAccount(account2).either
        found <- getAccount("13").either
        createdAnother <- createAccount(account3).either
        foundAll <- getAllAccounts.either
        deleted <- deleteAccount("13").either
      } yield
        assert(notFound)(isLeft(anything)) &&
          assert(created)(isRight(equalTo(account1))) &&
          assert(updated)(isRight(equalTo(true))) &&
          assert(found)(isRight(equalTo(account2))) &&
          assert(createdAnother)(isRight(equalTo(account3))) &&
          assert(foundAll)(isRight(equalTo(List(account2, account3)))) &&
          assert(deleted)(isRight(isTrue))
    }).provideSomeLayer[TestEnvironment](accountRepositoryLayer)
}