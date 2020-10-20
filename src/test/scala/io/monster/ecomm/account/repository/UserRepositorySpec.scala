package io.monster.ecomm.account.repository

import com.github.mlangc.slf4zio.api.LoggingSupport
import io.monster.ecomm.account.environment.TestEnvironments.usersRepositoryLayer
import io.monster.ecomm.account.model.{ User, UserNotFound }
import zio.test.Assertion.{ anything, isLeft, _ }
import zio.test.TestAspect.sequential
import zio.test._
import zio.test.environment.TestEnvironment

object UserRepositorySpec extends DefaultRunnableSpec with LoggingSupport {
  def spec =
    suite("User Persistence test1")(testM("User Persistence test1") {
      for {
        notFound       <- get(100).either
        created        <- create(User(13, "usr")).either
        updated        <- update(User(13, "newUsr")).either
        found          <- get(13).either
        recreated      <- create(User(13, "usr")).either
        createdAnother <- create(User(14, "usr2")).either
        foundAll       <- getAll.either
        updateFailed   <- update(User(1, "userNotThere")).either
        deleted        <- delete(13).either
        deleteFailed   <- delete(13).either
        allDeleted     <- delete(14).either
        empty          <- getAll.either
      } yield assert(notFound)(isLeft(anything)) &&
        assert(created)(isRight(equalTo(User(13, "usr")))) &&
        assert(updated)(isRight(equalTo(true))) &&
        assert(found)(isRight(equalTo(User(13, "newUsr")))) &&
        assert(recreated)(isRight(equalTo(User(13, "newUsr")))) &&
        assert(createdAnother)(isRight(equalTo(User(14, "usr2")))) &&
        assert(foundAll)(isRight(equalTo(List(User(13, "newUsr"), User(14, "usr2"))))) &&
        assert(updateFailed)(isLeft(equalTo(UserNotFound(1)))) &&
        assert(deleted)(isRight(isTrue)) &&
        assert(deleteFailed)(isLeft(equalTo(UserNotFound(13)))) &&
        assert(allDeleted)(isRight(isTrue)) &&
        assert(empty)(isRight(equalTo(List())))
    }).provideSomeLayer[TestEnvironment](usersRepositoryLayer) @@ sequential
}
