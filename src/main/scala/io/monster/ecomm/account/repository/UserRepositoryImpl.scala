package io.monster.ecomm.account.repository

import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.{LogHandler, Query0, Update0}
import io.monster.ecomm.account.model.{User, UserNotFound}
import io.monster.ecomm.account.repository.Repository.UserService
import zio.Task
import zio.interop.catz._

final case class UserRepositoryImpl(xa: HikariTransactor[Task]) extends UserService {
  def get(id: Long): Task[User] = {
    SQL.get(id)
      .option
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        maybeUser => Task.require(UserNotFound(id))(Task.succeed(maybeUser)))
  }

  def getAll: Task[List[User]] = {
    SQL.getAll().to[List].transact(xa).foldM(err => Task.fail(err), output => Task.succeed(output))
  }

  def create(user: User): Task[User] = {
    get(user.id).flatMap(
      user => Task.succeed(println("There is a duplicate, so not inserting")) *> Task.succeed(user)
    ).orElse(
      SQL.create(user).run.transact(xa).foldM(err => Task.fail(err), _ => Task.succeed(user))
    )
  }

  def delete(id: Long): Task[Boolean] =
    SQL
      .delete(id)
      .run
      .transact(xa)
      .fold(_ => false, _ => true)


  def update(user: User): Task[Boolean] = {
    get(user.id).flatMap(
      user1 => Task.succeed(println(s"Updating user ${user1} to ${user}")) *>
        SQL
          .update(user)
          .run
          .transact(xa)
          .fold(_ => false, _ => true)
    ).orElse(
      Task.fail(UserNotFound(user.id))
    )
  }
}

object SQL {

  def getAll(): Query0[User] =
    sql"""SELECT  id, name FROM USER""".queryWithLogHandler[User](LogHandler.jdkLogHandler)

  def get(id: Long): Query0[User] =
    sql"""SELECT  id, name FROM USER where id=${id}""".queryWithLogHandler[User](LogHandler.jdkLogHandler)

  def create(user: User): Update0 =
    sql"""INSERT INTO USER (id, name) VALUES (${user.id}, ${user.name})""".updateWithLogHandler(LogHandler.jdkLogHandler)

  def delete(id: Long): Update0 =
    sql"""DELETE FROM USER WHERE id = $id""".update

  def update(user: User): Update0 =
    sql"""UPDATE USER SET name = ${user.name} WHERE id = ${user.id}""".updateWithLogHandler(LogHandler.jdkLogHandler)
}







