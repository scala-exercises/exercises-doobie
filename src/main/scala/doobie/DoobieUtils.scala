package doobie

import java.util.UUID

import doobie.Model.Country
import doobie.imports._

import scalaz.concurrent.Task
import scalaz.std.iterable._

object DoobieUtils {

  val xa = DriverManagerTransactor[Task](
    driver = "org.h2.Driver",
    url = s"jdbc:h2:mem:doobie-exercises-${UUID.randomUUID().toString};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    user = "sa",
    pass = ""
  )

  val createCountryTable: ConnectionIO[Int] = {
    sql"""
        CREATE TABLE IF NOT EXISTS country (
        code        VARCHAR(64),
        name        VARCHAR(255),
        population  INT,
        gnp         DECIMAL(10,2)
        )
     """.update.run
  }

  val dropCountryTable: ConnectionIO[Int] = sql"""DROP TABLE IF EXISTS country""".update.run

  def insertCountries(countries: List[Country]): ConnectionIO[Int] =
    Update[Country]("insert into country (code, name, population, gnp) values (?,?,?,?)").updateMany(countries)
}
