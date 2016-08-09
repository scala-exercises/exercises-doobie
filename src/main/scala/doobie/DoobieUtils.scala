package doobie

import java.sql.Connection

import doobie.Model._
import doobie.free.{ drivermanager => FD }
import doobie.imports._

import scalaz.concurrent.Task
import scalaz._, Scalaz._

object DoobieUtils {

  class CustomTransactor[B](implicit beforeActions: ConnectionIO[B]) extends Transactor[Task] {
    val driver = "org.h2.Driver"
    def url    = "jdbc:h2:mem:"
    val user   = "sa"
    val pass   = ""

    val connect: Task[Connection] =
      Task.delay(Class.forName(driver)) *> FD.getConnection(url, user, pass).trans[Task]

    override val before = super.before <* beforeActions
  }

  object CountryTable {

    val createCountryTable: ConnectionIO[Int] =
      sql"""
          CREATE TABLE IF NOT EXISTS country (
          code        VARCHAR(64),
          name        VARCHAR(255),
          population  INT,
          gnp         DECIMAL(10,2)
          )
       """.update.run

    def insertCountries(countries: List[Country]): ConnectionIO[Int] =
      Update[Country]("insert into country (code, name, population, gnp) values (?,?,?,?)")
        .updateMany(countries)

    implicit val beforeActions = createCountryTable <* insertCountries(countries)

    // Transactor for single-use in-memory databases pre-populated with test data.
    val xa = new CustomTransactor
  }

  object PersonTable {

    val createPersonTable: ConnectionIO[Int] =
      sql"""
          CREATE TABLE IF NOT EXISTS person (
          id   IDENTITY,
          name VARCHAR NOT NULL UNIQUE,
          age  INT
          )
       """.update.run

    implicit val beforeActions = createPersonTable

    val xa = new CustomTransactor
  }
}
