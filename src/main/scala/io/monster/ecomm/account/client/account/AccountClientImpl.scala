package io.monster.ecomm.account.client.account

import io.circe.generic.auto._
import io.monster.ecomm.account.client.account.AccountClient.AccountClient
import io.monster.ecomm.account.configuration.Configuration.AccountClientConfig
import io.monster.ecomm.account.model.Account
import org.http4s.{ Method, Request, Uri }
import org.http4s.circe.CirceEntityCodec.{ circeEntityDecoder, circeEntityEncoder }
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import zio.{ Task, TaskManaged }
import zio.interop.catz._

final class AccountClientImpl(client: TaskManaged[Client[Task]], accountClientConfig: AccountClientConfig)
    extends AccountClient
    with Http4sClientDsl[Task] {

  private val baseUrl = s"http://${accountClientConfig.host}:${accountClientConfig.port}"

  override def get(id: String): Task[Account] = {
    val uri = Uri(path = s"$baseUrl/accounts/$id")
    client.use { c =>
      c.expect[Account](uri.toString())
    }
  }

  override def getAll: Task[List[Account]] = {
    val uri = Uri(path = s"$baseUrl/accounts")
    client.use { c =>
      c.expect[List[Account]](uri.toString())
    }
  }

  override def create(account: Account): Task[Account] = {
    val uri = Uri.unsafeFromString(s"$baseUrl/accounts")
    val postRequest = Request[Task](Method.POST, uri).withEntity[Account](account)
    client.use { c =>
      c.expect[Account](postRequest)
    }
  }

  override def delete(id: String): Task[Boolean] = {
    val uri = Uri.unsafeFromString(s"$baseUrl/accounts/$id")
    val deleteRequest = Request[Task](Method.DELETE, uri)
    client.use { c =>
      c.expect[Boolean](deleteRequest)
    }
  }

  override def update(account: Account): Task[Boolean] = {
    val uri = Uri.unsafeFromString(s"$baseUrl/accounts/${account.id}")
    val updateRequest = Request[Task](Method.PUT, uri).withEntity[Account](account)
    client.use { c =>
      c.expect[Boolean](updateRequest)
    }
  }
}
