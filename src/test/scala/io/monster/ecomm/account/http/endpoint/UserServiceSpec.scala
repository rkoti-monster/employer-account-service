package io.monster.ecomm.account.http.endpoint

import org.http4s._
import org.http4s.implicits._
import zio.{Task, _}
import zio.interop.catz._
import zio.test.{testM, _}
import zio.test.Assertion._
import zio.test.interop.CatsTestFunctions
import zio.test.interop.catz._
import TestAspect._
import Uri._
import io.circe.{Decoder, Encoder}
import io.monster.ecomm.account.environment.TestEnvironments.appEnvironmentLayer
import io.monster.ecomm.account.http.HelloService
import io.monster.ecomm.account.http.endpoint.Users.UserTask
import io.monster.ecomm.account.model.{User, UserNotFound}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import zio.test.environment.TestEnvironment
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import zio.test.ExecutionStrategy.Sequential

object UserServiceSpec extends DefaultRunnableSpec {
  override def spec = suite("routes suite")(
    testM("GetAll users initially returns an empty list") {
      val io = for {
        result <- Users.routes.run(Request[UserTask](Method.GET, uri"/users")).value
        body <- result.orNull.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
      } yield body
      assertM(io)(equalTo("[]"))
    },

    testM("Create user creates a user succesfully") {
      implicit val UserEncoder: Encoder[User] = deriveEncoder[User]
      val request = Request[UserTask](Method.POST, uri"/users").withEntity(User(1, "Howdy!"))
      val io = for {
        result <- Users.routes.run(request).value
        body <- result.orNull.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
      } yield body
      assertM(io)(equalTo("{\"id\":1,\"name\":\"Howdy!\"}"))
    },

    testM("Create two users") {
      implicit val UserEncoder: Encoder[User] = deriveEncoder[User]
      val request = Request[UserTask](Method.POST, uri"/users").withEntity(User(1, "Howdy!"))
      val request1 = Request[UserTask](Method.POST, uri"/users").withEntity(User(2, "Howdy1!"))
      val read = Request[UserTask](Method.GET, uri"/users")
      for {
        result <- Users.routes.run(request).value
        body1 <- result.orNull.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
        result1 <- Users.routes.run(request1).value
        body2 <- result1.orNull.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
        result3 <- Users.routes.run(read).value
        body3 <- result3.orNull.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
      } yield
        assert(body1)(equalTo("{\"id\":1,\"name\":\"Howdy!\"}")) &&
          assert(body2)(equalTo("{\"id\":2,\"name\":\"Howdy1!\"}")) &&
          assert(body3)(equalTo("[{\"id\":1,\"name\":\"Howdy!\"},{\"id\":2,\"name\":\"Howdy1!\"}]"))
    }).provideSomeLayer[TestEnvironment](appEnvironmentLayer) @@ sequential @@ noDelay
}
