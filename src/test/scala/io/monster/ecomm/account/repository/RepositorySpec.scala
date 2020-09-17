package io.monster.ecomm.account.repository

import io.monster.ecomm.account.model.{Account, User}
import zio.test.Assertion.{anything, isLeft}
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, suite, testM}
import zio.test.environment.TestEnvironment
import io.monster.ecomm.account.environment.TestEnvironments.usersRepositoryLayer
import zio.test.assert

object RepositorySpec extends DefaultRunnableSpec {
  def spec =
    suite("User Persistence test")(testM("User Persistence test") {
      val ec = concurrent.ExecutionContext.global
      for {
        notFound <- get(100).either
        created <- create(User(13, "usr")).either
        updated <- update(User(13, "newUsr")).either
        found <- get(13).either
        createdAnother <- create(User(14, "usr2")).either
        foundAll <- getAll.either
        deleted <- delete(13).either
        allDeleted <- delete(14).either
        empty <- getAll.either
      } yield
        assert(notFound)(isLeft(anything)) &&
          assert(created)(isRight(equalTo(User(13, "usr")))) &&
          assert(updated)(isRight(equalTo(true))) &&
          assert(found)(isRight(equalTo(User(13, "newUsr")))) &&
          assert(createdAnother)(isRight(equalTo(User(14, "usr2")))) &&
          assert(foundAll)(isRight(equalTo(List(User(13, "newUsr"), User(14, "usr2"))))) &&
          assert(deleted)(isRight(isTrue)) &&
          assert(allDeleted)(isRight(isTrue)) &&
          assert(empty)(isRight(equalTo(List())))
    }).provideSomeLayer[TestEnvironment](usersRepositoryLayer)
}

