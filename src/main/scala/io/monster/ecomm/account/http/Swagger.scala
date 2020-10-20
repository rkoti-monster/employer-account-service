package io.monster.ecomm.account.http

import org.http4s.rho.swagger.SwaggerMetadata
import org.http4s.rho.swagger.models.Info

object Swagger {
  val metadata: SwaggerMetadata = SwaggerMetadata(apiInfo =
    Info(title = "Ecomm Account Service", version = "0.0.1", description = Some("The account service"))
  )
}

object SwaggerTags {
  val internalApi = "internal"
  val userApi = "users"
  val accountApi = "accounts"
}
