package io.monster.ecomm.account.http.endpoint

import com.github.mlangc.slf4zio.api.LoggingSupport
import io.circe.generic.auto._
import io.monster.ecomm.account.environment.TestEnvironments.appEnvironmentLayer
import io.monster.ecomm.account.http.endpoint.Accounts.{ AccountTask, circeJsonDecoder, circeJsonEncoder }
import io.monster.ecomm.account.model.Account
import org.http4s._
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment.TestEnvironment
import zio.test.{ testM, _ }

object AccountServiceSpec extends DefaultRunnableSpec with LoggingSupport {

  val account1 = Account("1", "Howdy!", None, None, None, None, None, None)
  val newAccount1 = Account("1", "Well!", None, None, None, None, None, None)
  val account2 = Account("2", "Howdy1!", None, None, None, None, None, None)
  val nonExistingAccount = Account("100", "nonExistingAccount", None, None, None, None, None, None)

  override def spec =
    suite("routes suite")(
      testM("GetAll accounts initially returns an empty list") {
        for {
          resOpt <- Accounts.api.toRoutes().run(Request[AccountTask](Method.GET, uri"/accounts")).value
          res    <- ZIO.fromOption(resOpt)
          body   <- res.as[List[Account]]
        } yield assert(body)(isEmpty)
      },
      testM("Create user creates a user successfully") {
        val request = Request[AccountTask](Method.POST, uri"/accounts").withEntity(account1)
        for {
          maybeResult <- Accounts.api.toRoutes().run(request).value
          res         <- ZIO.fromOption(maybeResult)
          body        <- res.as[Account]
        } yield assert(body)(equalTo(account1))
      },
      testM("Create two accounts") {
        val request1 = Request[AccountTask](Method.POST, uri"/accounts").withEntity(account1)
        val request2 = Request[AccountTask](Method.POST, uri"/accounts").withEntity(account2)
        val read = Request[AccountTask](Method.GET, uri"/accounts")
        val readOne = Request[AccountTask](Method.GET, uri"/accounts/1")
        val readTwo = Request[AccountTask](Method.GET, uri"/accounts/2")
        val update = Request[AccountTask](Method.PUT, uri"/accounts/1").withEntity(newAccount1)
        val updateThatWillFail =
          Request[AccountTask](Method.PUT, uri"/accounts/100").withEntity(nonExistingAccount)
        val delete = Request[AccountTask](Method.DELETE, uri"/accounts/2")
        val deleteThatWillFail = Request[AccountTask](Method.DELETE, uri"/accounts/100")

        for {
          // Create Account
          maybeResult1 <- Accounts.api.toRoutes().run(request1).value
          res1         <- ZIO.fromOption(maybeResult1)
          body1        <- res1.as[Account]

          maybeResult2 <- Accounts.api.toRoutes().run(request2).value
          res2         <- ZIO.fromOption(maybeResult2)
          body2        <- res2.as[Account]

          // Recreate first account
          maybeResult1_1 <- Accounts.api.toRoutes().run(request1).value
          res1_1         <- ZIO.fromOption(maybeResult1_1)
          body1_1        <- res1_1.as[Account]

          maybeResult3 <- Accounts.api.toRoutes().run(read).value
          res3         <- ZIO.fromOption(maybeResult3)
          body3        <- res3.as[List[Account]]

          maybeResult4 <- Accounts.api.toRoutes().run(readOne).value
          res4         <- ZIO.fromOption(maybeResult4)
          body4        <- res4.as[Account]

          maybeResult5 <- Accounts.api.toRoutes().run(readTwo).value
          res5         <- ZIO.fromOption(maybeResult5)
          body5        <- res5.as[Account]

          maybeResult6 <- Accounts.api.toRoutes().run(update).value
          res6         <- ZIO.fromOption(maybeResult6)
          body6        <- res6.as[Boolean]

          maybeResult7 <- Accounts.api.toRoutes().run(read).value
          res7         <- ZIO.fromOption(maybeResult7)
          body7        <- res7.as[List[Account]]

          maybeResult8 <- Accounts.api.toRoutes().run(delete).value
          res8         <- ZIO.fromOption(maybeResult8)
          body8        <- res8.as[Boolean]

          maybeResult9 <- Accounts.api.toRoutes().run(read).value
          res9         <- ZIO.fromOption(maybeResult9)
          body9        <- res9.as[List[Account]]

          maybeResult10 <- Accounts.api.toRoutes().run(updateThatWillFail).value
          res10         <- ZIO.fromOption(maybeResult10)
          body10        <- res10.as[String]

          maybeResult11 <- Accounts.api.toRoutes().run(deleteThatWillFail).value
          res11         <- ZIO.fromOption(maybeResult11)
          body11        <- res11.as[String]

        } yield assert(body1)(equalTo(account1)) &&
          assert(body2)(equalTo(account2)) &&
          assert(body1_1)(equalTo(account1)) &&
          assert(body3.toSet)(equalTo(Set(account1, account2))) &&
          assert(body4)(equalTo(account1)) &&
          assert(body5)(equalTo(account2)) &&
          assert(body6)(isTrue) &&
          assert(body7.toSet)(equalTo(Set(account2, newAccount1))) &&
          assert(body8)(isTrue) &&
          assert(body9)(equalTo(List(newAccount1))) &&
          assert(body10)(equalTo("Account not found")) &&
          assert(body11)(equalTo("Account not found"))

      }
    ).provideSomeLayer[TestEnvironment](appEnvironmentLayer) @@ sequential
}
