package io.monster.ecomm.account.repository

import com.github.mlangc.slf4zio.api.{ LoggingSupport, Slf4jLoggerOps }
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.getquill.{ idiom => _ }
import io.monster.ecomm.account.model.schema._
import io.monster.ecomm.account.model.{ User, UserNotFound }
import io.monster.ecomm.account.repository.UserRepository.UserService
import zio.Task
import zio.interop.catz._

final case class UserRepositoryImpl(xa: HikariTransactor[Task]) extends UserService with LoggingSupport {
  def get(id: Long): Task[User] =
    SQL
      .get(id)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        {
          case Nil       => Task.fail(UserNotFound(1))
          case user :: _ => Task.succeed(user)
        }
      )

  def getAll: Task[List[User]] =
    SQL.getAll.transact(xa).foldM(err => Task.fail(err), output => Task.succeed(output))

  def create(user: User): Task[User] =
    get(user.id)
      .flatMap(user => logger.infoIO(s"Not inserting as $user already exists") *> Task.succeed(user))
      .orElse(SQL.create(user).transact(xa).foldM(err => Task.fail(err), _ => Task.succeed(user)))

  def delete(id: Long): Task[Boolean] =
    get(id).foldM(
      _ => Task.fail(UserNotFound(id)),
      user =>
        logger.infoIO(s"Deleting ${user}")
          *> SQL.delete(id).transact(xa).fold(_ => false, _ => true)
    )

  def update(user: User): Task[Boolean] =
    get(user.id)
      .flatMap(existingUser =>
        logger.infoIO(s"Updating user ${existingUser} to ${user}") *>
          SQL
            .update(user)
            .transact(xa)
            .fold(_ => false, _ => true)
      )
      .orElse(Task.fail(UserNotFound(user.id)))
}

object SQL {

  import dc._

  def getAll: ConnectionIO[List[User]] =
    run(quote {
      user
    })

  def get(id: Long): ConnectionIO[List[User]] =
    run(quote {
      user.filter(_.id == lift(id))
    })

  def create(userIn: User): ConnectionIO[Long] =
    run(quote {
      user.insert(lift(userIn))
    })

  def delete(id: Long): ConnectionIO[Long] =
    run(quote {
      user.filter(_.id == lift(id)).delete
    })

  def update(userIn: User): ConnectionIO[Long] =
    run(quote {
      user.filter(_.id == lift(userIn.id)).update(_.name -> lift(userIn.name))
    })

}
