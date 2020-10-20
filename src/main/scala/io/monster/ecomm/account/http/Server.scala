package io.monster.ecomm.account.http

import cats.data.Kleisli
import zio.ExitCode
import cats.effect.{ ExitCode => CatsExitCode }
import cats.implicits._
import io.monster.ecomm.account.configuration.Configuration.HttpServerConfig
import io.monster.ecomm.account.environment.AppEnvironment
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{ AutoSlash, GZip }
import org.http4s.{ HttpRoutes, Request, Response }
import zio.interop.catz._
import zio.{ RIO, ZIO }
import io.monster.ecomm.account.http.endpoint.Users
import io.monster.ecomm.account.http.endpoint.Accounts

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object Server {
  type ServerRIO[A] = RIO[AppEnvironment, A]
  type ServerRoutes = Kleisli[ServerRIO, Request[ServerRIO], Response[ServerRIO]]

  def runServer: ZIO[AppEnvironment, Throwable, ExitCode] =
    ZIO
      .runtime[AppEnvironment]
      .flatMap { implicit rts =>
        val cfg = rts.environment.get[HttpServerConfig]
        val ec = rts.platform.executor.asEC

        BlazeServerBuilder[ServerRIO](ec)
          .bindHttp(cfg.port, cfg.host)
          .withHttpApp(createRoutes(cfg.path))
          .serve
          .compile[ServerRIO, ServerRIO, CatsExitCode]
          .fold(ExitCode.success)((_, catsCode) => ExitCode(catsCode.code))
      }

  def createRoutes(basePath: String): ServerRoutes =
    Router[ServerRIO](basePath -> middleware(getSwaggerRoute)).orNotFound

  private def getSwaggerRoute = {
    import org.http4s.rho._
    import org.http4s.rho.swagger._

    val swaggerMiddleWare: RhoMiddleware[ServerRIO] = SwaggerSupport[ServerRIO]
      .createRhoMiddleware(swaggerMetadata = Swagger.metadata)

    Users.api.and(Accounts.api).toRoutes(swaggerMiddleWare);
  }

  private val middleware: HttpRoutes[ServerRIO] => HttpRoutes[ServerRIO] = { http: HttpRoutes[ServerRIO] =>
    AutoSlash(http)
  }.andThen { http: HttpRoutes[ServerRIO] =>
    GZip(http)
  }
}
