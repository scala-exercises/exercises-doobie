package doobie

import java.sql.Connection

import doobie.Model._
import doobie.free.{ drivermanager => FD }
import doobie.imports._

import scalaz.concurrent.Task
import scalaz._, Scalaz._

object DoobieUtils {

  // Transactor for single-use in-memory databases pre-populated with test data.
  val xa = new Transactor[Task] {

    val driver = "org.h2.Driver"
    def url    = "jdbc:h2:mem:"
    val user   = "sa"
    val pass   = ""

    val connect: Task[Connection] =
      Task.delay(Class.forName(driver)) *> FD.getConnection(url, user, pass).trans[Task]

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
      Update[Country](s"insert into country (code, name, population, gnp) values (?,?,?,?)").updateMany(countries)

    override val before = 
      super.before <* createCountryTable <* insertCountries(countries)

  }

}
