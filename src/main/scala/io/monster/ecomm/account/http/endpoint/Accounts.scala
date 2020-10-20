package io.monster.ecomm.account.http.endpoint

import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder }
import io.monster.ecomm.account.environment.AppEnvironment
import io.monster.ecomm.account.model.{ Account, AccountNotFound }
import io.monster.ecomm.account.repository._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }
import zio.RIO
import zio.interop.catz._
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.rho.RhoRoutes
import io.monster.ecomm.account.http.SwaggerTags._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Accounts {
  type AccountTask[A] = RIO[AppEnvironment, A]

  private val prefixPath = "/accounts"
  private val accountNotFound = "Account not found"
  private val errorOccurred = "Error occurred"

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[AccountTask, A] =
    jsonOf[AccountTask, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[AccountTask, A] =
    jsonEncoderOf[AccountTask, A]

  private val swaggerSupport = SwaggerSupport.apply[AccountTask]
  import swaggerSupport._

  val api: RhoRoutes[AccountTask] = new RhoRoutes[AccountTask] {
    "Get all accounts" **
      List(internalApi, accountApi) @@
      GET / prefixPath |>> { () =>
        getAllAccounts.foldM(err => InternalServerError(errorOccurred), accounts => Ok(accounts))
      }

    "Get account by account id" **
      accountApi @@
      GET / prefixPath / pathVar[String]("accountId", "Unique account ID") |>> { (accountId: String) =>
        getAccount(accountId).foldM(
          {
            case _: AccountNotFound => InternalServerError(accountNotFound)
            case _: Throwable       => InternalServerError(errorOccurred)
          },
          account => Ok(account)
        )
      }

    "Create a new account" **
      accountApi @@
      POST / prefixPath ^ EntityDecoder[AccountTask, Account] |>> { (account: Account) =>
        createAccount(account).foldM(err => InternalServerError(errorOccurred), _ => Created(account))
      }

    "Delete account by ID" **
      List(internalApi, accountApi) @@
      DELETE / prefixPath / pathVar[String]("accountId", "Unique Account Id") |>> { (accountId: String) =>
        deleteAccount(accountId).foldM(
          {
            case _: AccountNotFound => InternalServerError(accountNotFound)
            case _: Throwable       => InternalServerError(errorOccurred)
          },
          _ => Ok(true)
        )
      }

    "Update account" ** accountApi @@
      PUT / prefixPath / pathVar[String]("accountId", "Account Id") ^ EntityDecoder[
        AccountTask,
        Account
      ] |>> { (accountId: String, account: Account) =>
        updateAccount(account.copy(id = accountId)).foldM(
          {
            case _: AccountNotFound => InternalServerError(accountNotFound)
            case _: Throwable       => InternalServerError(errorOccurred)
          },
          _ => Ok(true)
        )
      }
  }

}
