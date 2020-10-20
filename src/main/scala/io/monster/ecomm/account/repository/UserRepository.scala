package io.monster.ecomm.account.repository

import io.monster.ecomm.account.db.transactor.DbTransactor
import io.monster.ecomm.account.model.User
import zio.{ Task, URLayer, ZLayer }

object UserRepository {

  trait UserService {
    def get(id: Long): Task[User]

    def getAll: Task[List[User]]

    def create(user: User): Task[User]

    def delete(id: Long): Task[Boolean]

    def update(user: User): Task[Boolean]
  }

  val live: URLayer[DbTransactor, UserRepository] =
    ZLayer.fromFunction { resource =>
      UserRepositoryImpl(resource.get.xa)
    }
}
