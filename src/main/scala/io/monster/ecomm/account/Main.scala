package io.monster.ecomm.account

import cats.effect.ExitCode
import io.monster.ecomm.account.environment.Environments.appEnvironmentLayer
import io.monster.ecomm.account.http.Server
import zio._
import zio.interop.catz._
import org.http4s.implicits._
import zio._

object Main extends App {
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program = for {
      _ <- Server.runServer
    } yield ()
    program.provideLayer(appEnvironmentLayer).foldM(_ => IO.succeed(1), _ => IO.succeed(0))
  }
}


