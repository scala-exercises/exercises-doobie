package doobie

import doobie.imports._
import doobie.free.{ drivermanager => FD }

import java.sql.Connection

import scalaz.concurrent.Task
import scalaz._, Scalaz._

object UpdatesSectionHelpers {

  case class Person(id: Long, name: String, age: Option[Short])

  val xa = new Transactor[Task] {

    val driver = "org.h2.Driver"
    def url    = "jdbc:h2:mem:"
    val user   = "sa"
    val pass   = ""

    val connect: Task[Connection] =
      Task.delay(Class.forName(driver)) *> FD.getConnection(url, user, pass).trans[Task]

    val create: Update0 =
      sql"""
        CREATE TABLE person (
        id   IDENTITY,
        name VARCHAR NOT NULL UNIQUE,
        age  INT)
    """.update

    override val before =
      super.before <* create.run

  }

  def insert1(name: String, age: Option[Int]): Update0 =
    sql"""
        INSERT INTO person (name, age) VALUES ($name, $age)
    """.update
}
