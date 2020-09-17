package io.monster.ecomm.account.model

final case class User(id: Long, name: String)

final case class UserNotFound(id: Long) extends Exception

final case class AccountNotFound(id: String) extends Exception

final case class Account(
                          id: String,
                          name: String,
                          contactId: Option[String],
                          zuoraId: Option[String],
                          crmId: Option[String],
                          website: Option[String],
                          parentAccountId: Option[String],
                          address: Option[String]
                        )