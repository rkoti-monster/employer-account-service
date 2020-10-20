package io.monster.ecomm.account.http.endpoint

import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder }
import io.monster.ecomm.account.environment.AppEnvironment
import io.monster.ecomm.account.model.{ User, UserNotFound }
import io.monster.ecomm.account.repository._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }
import zio.RIO
import zio.interop.catz._
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.rho.RhoRoutes
import io.monster.ecomm.account.http.SwaggerTags._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Users {
  type UserTask[A] = RIO[AppEnvironment, A]

  private val prefixPath = "/users"
  private val userNotFound = "User not found"
  private val errorOccurred = "Error occurred"

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] =
    jsonOf[UserTask, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]

  private val swaggerSupport = SwaggerSupport.apply[UserTask]
  import swaggerSupport._

  val api: RhoRoutes[UserTask] = new RhoRoutes[UserTask] {
    "Get all users" **
      List(internalApi, userApi) @@
      GET / prefixPath |>> { () =>
        getAll.foldM(err => InternalServerError(errorOccurred), users => Ok(users))
      }

    "Find user with user id" **
      userApi @@
      GET / prefixPath / pathVar[Int]("userId", ApiDescription.userId) |>> { (userId: Int) =>
        get(userId).foldM(
          {
            case _: UserNotFound => InternalServerError(userNotFound)
            case _: Throwable    => InternalServerError(errorOccurred)
          },
          user => Ok(user)
        )
      }

    "Delete user with user id" **
      userApi @@
      DELETE / prefixPath / pathVar[Int]("userId", ApiDescription.userId) |>> { (userId: Int) =>
        delete(userId).foldM(
          {
            case _: UserNotFound => InternalServerError(userNotFound)
            case _: Throwable    => InternalServerError(errorOccurred)
          },
          user => Ok(user)
        )
      }

    "Create a new user" **
      userApi @@
      POST / prefixPath ^ EntityDecoder[UserTask, User] |>> { (body: User) =>
        create(body).foldM(err => InternalServerError(errorOccurred), _ => Created(body))
      }

    "Update user entity with user id" **
      userApi @@
      PUT / prefixPath / pathVar[Int]("userId", ApiDescription.userId) ^ EntityDecoder[UserTask, User] |>> {
        (userId: Int, body: User) =>
          update(body).foldM(
            {
              case _: UserNotFound => InternalServerError(userNotFound)
              case _: Throwable    => InternalServerError(errorOccurred)
            },
            user => Ok(user)
          )
      }
  }
}

object ApiDescription {
  val userId = "User ID"
}
