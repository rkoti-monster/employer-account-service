package io.monster.ecomm.account.client.http

import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.CanFail.canFailAmbiguous1
import zio.interop.catz._
import zio._

import scala.concurrent.ExecutionContext.Implicits

object HttpClient {
  def makeHttpClient: UIO[TaskManaged[Client[Task]]] =
    ZIO.runtime[Any].map { implicit rts =>
      BlazeClientBuilder
        .apply[Task](Implicits.global)
        .resource
        .toManaged
    }

  val httpClientLive: ULayer[Has[TaskManaged[Client[Task]]]] = HttpClient.makeHttpClient.toLayer.orDie
}
