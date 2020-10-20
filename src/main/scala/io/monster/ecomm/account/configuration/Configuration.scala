package io.monster.ecomm.account.configuration

import pureconfig.ConfigSource
import zio.{ Has, Task, ULayer, ZIO, ZLayer }
import pureconfig.generic.auto._

object Configuration {

  final case class AppConfig(
    httpServer: HttpServerConfig,
    database: DbConfig,
    accountClientConfig: AccountClientConfig
  )

  final case class HttpServerConfig(host: String, port: Int, path: String)

  final case class DbConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

  final case class AccountClientConfig(host: String, port: String)

  val live: ULayer[Configuration] = {
    val _ = Class.forName("com.mysql.cj.jdbc.Driver")
    ZLayer.fromEffectMany(
      ZIO
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map { c =>
          Has(c.database) ++ Has(c.httpServer) ++ Has(c.accountClientConfig)
        }
        .orDie
    )
  }

  val test: ULayer[Configuration] = ZLayer.fromEffectMany(
    Task.effectTotal(
      Has(HttpServerConfig("0.0.0.0", 8083, "")) ++
        Has(
          DbConfig(
            "org.h2.Driver",
            "jdbc:h2:~/test;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:h2-schema.sql';",
            user = "",
            password = "",
            threadPoolSize = 1
          )
        ) ++
        Has(AccountClientConfig("localhost", "8083"))
    )
  )

  def testWithDb(host: String, port: Int): ZLayer[Any, Nothing, Configuration] = {
    val _ = Class.forName("com.mysql.cj.jdbc.Driver")
    ZLayer.fromEffectMany(
      Task.effectTotal(
        Has(HttpServerConfig("0.0.0.0", 8083, "")) ++
          Has(
            DbConfig(
              "",
              s"jdbc:mysql://$host:$port/users?autoReconnect=true&useSSL=false&characterEncoding=utf8&useUnicode=true&serverTimezone=UTC",
              user = "user",
              password = "user",
              threadPoolSize = 1
            )
          ) ++
          Has(AccountClientConfig("localhost", "8083"))
      )
    )
  }
}
