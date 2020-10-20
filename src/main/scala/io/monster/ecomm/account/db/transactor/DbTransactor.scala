package io.monster.ecomm.account.db.transactor

import cats.effect.Blocker
import com.github.mlangc.slf4zio.api.LoggingSupport
import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import doobie.hikari.HikariTransactor
import io.monster.ecomm.account.app.Main.platform
import io.monster.ecomm.account.db.DBConfig
import zio.interop.catz.{ taskConcurrentInstance, zioContextShift }
import zio.{ Task, URLayer, ZLayer }

object DbTransactor extends LoggingSupport {

  trait Resource {
    val xa: HikariTransactor[Task]
  }

  val live: URLayer[DBConfig, DbTransactor] = ZLayer.fromService { db =>
    new Resource {
      val xa: HikariTransactor[Task] = {
        val config = new HikariConfig()
        config.setJdbcUrl(db.url)
        config.setUsername(db.user)
        config.setPassword(db.password)
        config.setMaximumPoolSize(db.threadPoolSize)
        val datasource = new HikariDataSource(config)
        HikariTransactor.apply[Task](
          datasource,
          platform.executor.asEC,
          Blocker.liftExecutionContext(platform.executor.asEC)
        )
      }
    }
  }
}
