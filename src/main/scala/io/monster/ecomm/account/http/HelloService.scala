package io.monster.ecomm.account.http

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s.implicits._

object HelloService {
  private val dsl = Http4sDsl[Task]

  import dsl._

  val service = HttpRoutes.of[Task] {
    case GET -> Root => {
      Ok("hello!")
    }
  }
}
