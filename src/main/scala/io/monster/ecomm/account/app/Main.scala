package io.monster.ecomm.account.app

import com.github.mlangc.slf4zio.api.{ LoggingSupport, Slf4jLoggerOps }
import io.monster.ecomm.account.environment.Environments.appEnvironmentLayer
import io.monster.ecomm.account.http.Server
import zio.{ App, ExitCode, UIO, ZEnv, ZIO }

object Main extends App with LoggingSupport {
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program = for {
      exitCode <- Server.runServer
    } yield exitCode
    program
      .provideLayer(appEnvironmentLayer)
      .foldCauseM(
        cause => logger.errorIO(cause.prettyPrint).as(ExitCode.failure),
        exitCode => UIO.effectTotal(exitCode)
      )
  }
}
