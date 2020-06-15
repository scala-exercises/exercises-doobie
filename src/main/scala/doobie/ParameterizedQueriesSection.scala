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

import cats.data.NonEmptyList
import DoobieUtils.CountryTable._
import ParameterizedQueryHelpers._
import org.scalaexercises.definitions.Section
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Previously we have worked with static SQL queries where the values used to filter data were
 * hard-coded and didn't change.
 *
 * {{{
 *   select code, name, population, gnp from country where code = "GBR"
 * }}}
 *
 * In this section, we'll learn how to construct parameterized queries.
 *
 * We’re still playing with the country table, shown here for reference.
 * {{{
 * code    name                      population    gnp
 * "DEU"  "Germany"                    82164700    2133367.00
 * "ESP"  "Spain"                      39441700          null
 * "FRA"  "France",                    59225700    1424285.00
 * "GBR"  "United Kingdom"             59623400    1378330.00
 * "USA"  "United States of America"  278357000    8510700.00
 * }}}
 *
 * To make simpler the code we built a method which prepares the database, makes the query and transacts
 * it all:
 * {{{
 * def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
 *    transactor.use((createCountryTable *> insertCountries(countries) *> f).transact[IO])
 * }}}
 *
 * @param name parameterized_queries
 */
object ParameterizedQueriesSection extends AnyFlatSpec with Matchers with Section {

  /**
   * == Adding a Parameter ==
   *
   * Let’s factor our query into a method and add a parameter that selects only the countries with
   * a population larger than some value the user will provide. We insert the minPop argument into
   * our SQL statement as $minPop, just as if we were doing string interpolation.
   * {{{
   *   def biggerThan(minPop: Int) =
   *   sql"""
   *     select code, name, population, gnp
   *     from country
   *     where population > $minPop
   *     order by population asc
   *   """.query[Country]
   * }}}
   */
  def addingAParameter(res0: String, res1: String) = {

    val countriesName = transactorBlock(biggerThan(75000000).to[List])
      .unsafeRunSync()
      .map(_.name)

    countriesName should be(List(res0, res1))
  }

  /**
   * So what’s going on? It looks like we’re just dropping a string literal into our SQL string,
   * but actually we’re constructing a proper parameterized PreparedStatement, and the minProp
   * value is ultimately set via a call to setInt
   *
   * '''doobie''' allows you to interpolate values of any type (and options thereof) with a `Put`
   * instance, which includes:
   *  - any JVM type that has a target mapping defined by the JDBC specification,
   *  - vendor-specific types defined by extension packages,
   *  - custom column types that you define, and
   *  - single-member products (case classes, typically) of any of the above.
   *
   * == Multiple Parameters ==
   *
   * Multiple parameters work the same way.
   * {{{
   *   def populationIn(range: Range) =
   *   sql"""
   *     select code, name, population, gnp
   *     from country
   *     where population > ${range.min} and population < ${range.max}
   *     order by population asc
   *   """.query[Country]
   * }}}
   */
  def addingMultipleParameters(res0: String, res1: String, res2: String) = {

    val countriesName = transactorBlock(populationIn(25000000 to 75000000).to[List])
      .unsafeRunSync()
      .map(_.name)

    countriesName should be(List(res0, res1, res2))
  }

  /**
   * == Dealing with IN Clauses ==
   *
   * A common irritant when dealing with SQL literals is the desire to inline a sequence of
   * arguments into an IN clause, but SQL does not support this notion (nor does JDBC do anything
   * to assist). doobie supports this via statement fragments.
   * {{{
   *   def populationIn(range: Range, codes: NonEmptyList[String]) = {
   *     val q = fr"""
   *        select code, name, population, gnp
   *        from country
   *        where population > ${range.min}
   *        and   population < ${range.max}
   *        and   """ ++ Fragments.in(fr"code", codes) // code IN (...)
   *     q.query[Country]
   *   }
   * }}}
   *
   * Note that the `IN` clause must be non-empty, so `codes` is a `NonEmptyList`.
   */
  def dealingWithInClause(res0: String, res1: String) = {

    val countriesName = transactorBlock(
      populationIn(25000000 to 75000000, NonEmptyList.of("ESP", "USA", "FRA"))
        .to[List]
    ).unsafeRunSync()
      .map(_.name)

    countriesName should be(List(res0, res1))
  }
}
