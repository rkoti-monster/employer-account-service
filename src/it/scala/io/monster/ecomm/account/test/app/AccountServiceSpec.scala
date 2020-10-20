package io.monster.ecomm.account.test.app

import io.monster.ecomm.account.client._
import io.monster.ecomm.account.client.account.AccountClient.accountClientLiveLayer
import io.monster.ecomm.account.client.http.HttpClient.httpClientLive
import io.monster.ecomm.account.configuration.Configuration.AccountClientConfig
import io.monster.ecomm.account.model.Account
import io.monster.ecomm.account.test.util.Constants.{
  easPortName,
  easResourceFileName,
  easServiceName,
  mysqlInitConfigMapName,
  mysqlInitFileName,
  mysqlResourceFileName,
  mysqlServiceName
}
import io.monster.ecomm.account.test.util.K8sHelper.{ deleteEnv, _ }
import zio._
import zio.test.Assertion.{ equalTo, isEmpty }
import zio.test.TestAspect.sequential
import zio.test._

import scala.util.Try

object AccountServiceSpec extends DefaultRunnableSpec {
  override def spec = suite("Main")(accountSuite)

  /**
   * Integration test that uses frabrik8 kubernetes API to deploy account service and mysql db on to a minikube cluster
   * It uses AccountClient to post crud commands and asserts on the expected outcomes
   */
  private val accountSuite = {
    val client = createConfigMap(mysqlInitConfigMapName, mysqlInitFileName)
    startTestEnv(mysqlResourceFileName, Some(client))
    startTestEnv(easResourceFileName, Some(client))
    val endpoint = getEndpoint(client, easServiceName, easPortName)
    val account = Account("13", "usr", None, None, None, None, None, None)
    val anotherAccount = Account("13", "usr1", None, None, None, None, None, None)

    // This is needed to wait for the services to be up and running
    // Will be improved in the coming days
    Thread.sleep(30000)

    suite("AccountService")(
      testM("Should honour CRUD operations") {
        for {
          res     <- getAll
          acc     <- create(account)
          res1    <- getAll
          update  <- update(anotherAccount)
          updated <- get(account.id)
          del     <- delete(account.id)
          res2    <- getAll

        } yield assert(res)(isEmpty) &&
          assert(acc)(equalTo(account)) &&
          assert(res1)(equalTo(List(account))) &&
          assert(del)(equalTo(true)) &&
          assert(res2)(isEmpty) &&
          assert(update)(equalTo(true)) &&
          assert(updated)(equalTo(anotherAccount))
      },
      testM("Delete Environment") {
        for {
          _ <- ZIO.fromTry(Try(deleteEnv(client, mysqlServiceName, Some(mysqlInitConfigMapName))))
          _ <- ZIO.fromTry(Try(deleteEnv(client, easServiceName)))
          _ <- ZIO.fromTry(Try(Thread.sleep(15000)))
        } yield assertCompletes
      }
    ).provideLayer(
      (ZLayer.succeed(
        AccountClientConfig(endpoint._1, endpoint._2.toString)
      ) ++ httpClientLive) >>> accountClientLiveLayer
    ) @@ sequential
  }
}
