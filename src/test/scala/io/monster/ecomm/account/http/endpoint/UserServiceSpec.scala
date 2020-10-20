package io.monster.ecomm.account.http.endpoint

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.monster.ecomm.account.environment.TestEnvironments.appEnvironmentLayer
import io.monster.ecomm.account.http.endpoint.Users.{ UserTask, circeJsonDecoder }
import io.monster.ecomm.account.model.User
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment.TestEnvironment
import zio.test.{ testM, _ }

object UserServiceSpec extends DefaultRunnableSpec {

  val user1 = User(1, "Howdy!");
  val user2 = User(2, "Howdy1!")
  val newUser1 = User(1, "Well!")
  val nonExistingUser = User(100, "notThere")

  override def spec =
    suite("routes suite")(
      testM("GetAll users initially returns an empty list") {
        for {
          resOpt <- Users.api.toRoutes().run(Request[UserTask](Method.GET, uri"/users")).value
          res    <- ZIO.fromOption(resOpt)
          body   <- res.as[List[User]]
        } yield assert(body)(isEmpty)
      },
      testM("Create user creates a user successfully") {
        implicit val UserEncoder: Encoder[User] = deriveEncoder[User]
        val request = Request[UserTask](Method.POST, uri"/users").withEntity(user1)
        for {
          maybeResult <- Users.api.toRoutes().run(request).value
          res         <- ZIO.fromOption(maybeResult)
          body        <- res.as[User]
        } yield assert(body)(equalTo(user1))
      },
      testM("Create two users") {
        implicit val UserEncoder: Encoder[User] = deriveEncoder[User]
        val request1 = Request[UserTask](Method.POST, uri"/users").withEntity(user1)
        val request2 = Request[UserTask](Method.POST, uri"/users").withEntity(user2)
        val read = Request[UserTask](Method.GET, uri"/users")
        val readOne = Request[UserTask](Method.GET, uri"/users/1")
        val readTwo = Request[UserTask](Method.GET, uri"/users/2")
        val update = Request[UserTask](Method.PUT, uri"/users/1").withEntity(newUser1)
        val updateThatWillFail = Request[UserTask](Method.PUT, uri"/users/100").withEntity(nonExistingUser)
        val delete = Request[UserTask](Method.DELETE, uri"/users/2")
        val deleteThatWillFail = Request[UserTask](Method.DELETE, uri"/users/100")

        for {
          maybeResult1 <- Users.api.toRoutes().run(request1).value
          res1         <- ZIO.fromOption(maybeResult1)
          body1        <- res1.as[User]

          maybeResult2 <- Users.api.toRoutes().run(request2).value
          res2         <- ZIO.fromOption(maybeResult2)
          body2        <- res2.as[User]

          maybeResult1_1 <- Users.api.toRoutes().run(request1).value
          res1_1         <- ZIO.fromOption(maybeResult1_1)
          body1_1        <- res1_1.as[User]

          maybeResult3 <- Users.api.toRoutes().run(read).value
          res3         <- ZIO.fromOption(maybeResult3)
          body3        <- res3.as[List[User]]

          maybeResult4 <- Users.api.toRoutes().run(readOne).value
          res4         <- ZIO.fromOption(maybeResult4)
          body4        <- res4.as[User]

          maybeResult5 <- Users.api.toRoutes().run(readTwo).value
          res5         <- ZIO.fromOption(maybeResult5)
          body5        <- res5.as[User]

          maybeResult6 <- Users.api.toRoutes().run(update).value
          res6         <- ZIO.fromOption(maybeResult6)
          body6        <- res6.as[Boolean]

          maybeResult7 <- Users.api.toRoutes().run(read).value
          res7         <- ZIO.fromOption(maybeResult7)
          body7        <- res7.as[List[User]]

          maybeResult8 <- Users.api.toRoutes().run(delete).value
          res8         <- ZIO.fromOption(maybeResult8)
          body8        <- res8.as[Boolean]

          maybeResult9 <- Users.api.toRoutes().run(read).value
          res9         <- ZIO.fromOption(maybeResult9)
          body9        <- res9.as[List[User]]

          maybeResult10 <- Users.api.toRoutes().run(updateThatWillFail).value
          res10         <- ZIO.fromOption(maybeResult10)
          body10        <- res10.as[String]

          maybeResult11 <- Users.api.toRoutes().run(deleteThatWillFail).value
          res11         <- ZIO.fromOption(maybeResult11)
          body11        <- res11.as[String]

        } yield assert(body1)(equalTo(user1)) &&
          assert(body2)(equalTo(user2)) &&
          assert(body1_1)(equalTo(user1)) &&
          assert(body3.toSet)(equalTo(Set(user1, user2))) &&
          assert(body4)(equalTo(user1)) &&
          assert(body5)(equalTo(user2)) &&
          assert(body6)(isTrue) &&
          assert(body7.toSet)(equalTo(Set(user2, newUser1))) &&
          assert(body8)(isTrue) &&
          assert(body9)(equalTo(List(newUser1))) &&
          assert(body10)(equalTo("User not found")) &&
          assert(body11)(equalTo("User not found"))
      }
    ).provideSomeLayer[TestEnvironment](appEnvironmentLayer) @@ sequential
}
