/*
 * Copyright 2016-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doobielib

import cats.effect._
import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import Model._

object DoobieUtils {

  val transactor: Resource[IO, H2Transactor[IO]] = {
    def url  = "jdbc:h2:mem:"
    val user = "sa"
    val pass = ""

    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](1)
      xa <- H2Transactor.newH2Transactor[IO](url, user, pass, ec)
    } yield xa
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

    def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
      transactor.use((createCountryTable *> insertCountries(countries) *> f).transact[IO])
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

    def transactorBlock[A](f: => ConnectionIO[A]) =
      transactor.use((createPersonTable *> f).transact[IO])
  }
}
