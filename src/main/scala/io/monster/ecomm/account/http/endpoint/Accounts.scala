package io.monster.ecomm.account.http.endpoint

import io.circe.{Decoder, Encoder}
import io.monster.ecomm.account.model.Account
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import zio.{RIO, Task}
import zio.interop.catz._
import io.circe.generic.auto._
import io.monster.ecomm.account
import io.monster.ecomm.account.environment.Environments.AppEnvironment
import io.monster.ecomm.account.repository.UserRepositoryImpl
import io.monster.ecomm.account.repository._
import org.http4s.Method.UPDATE
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.server.Router

object Accounts {
  type AccountTask[A] = RIO[AppEnvironment, A]

  private val prefixPath = "/accounts"

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[AccountTask, A] = jsonOf[AccountTask, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[AccountTask, A] = jsonEncoderOf[AccountTask, A]

  val dsl: Http4sDsl[AccountTask] = Http4sDsl[AccountTask]

  import dsl._

  val accountRoutes =
    HttpRoutes.of[AccountTask] {
      case GET -> Root =>
        Ok(getAllAccounts)
      case GET -> Root / id =>
        Ok(getAccount(id))
      case request@POST -> Root =>
        request.decode[Account] {
          account => Created(createAccount(account))
        }
      case DELETE -> Root / id => Ok(deleteAccount(id))
      case request@PUT -> Root / id =>
        request.decode[Account] {
          account => Ok(updateAccount(account))
        }
    }

  val routes: HttpRoutes[AccountTask] = Router(
    prefixPath -> accountRoutes
  )
}
