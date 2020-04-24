/*
 * Copyright 2016-2020 47 Degrees <https://47deg.com>
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

import doobie.implicits._
import DoobieUtils.CountryTable._
import org.scalaexercises.definitions.Section
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * We are going to construct some programs that retrieve data from the database and stream it back,
 * mapping to Scala types on the way.
 *
 * We will be playing with the country table that has the following structure:
 * {{{
 * CREATE TABLE country (
 * code       character(3)  NOT NULL,
 * name       text          NOT NULL,
 * population integer       NOT NULL,
 * gnp        numeric(10,2)
 * )
 * }}}
 *
 * For the exercises, the ''country'' table will contain:
 * {{{
 * code    name                      population    gnp
 * "DEU"  "Germany"                    82164700    2133367.00
 * "ESP"  "Spain"                      39441700          null
 * "FRA"  "France"                     59225700    1424285.00
 * "GBR"  "United Kingdom"             59623400    1378330.00
 * "USA"  "United States of America"  278357000    8510700.00
 * }}}
 *
 * == How to select data ==
 *
 * As we commented in the previous section, the `sql` string interpolator allows us to create a
 * query to select data from the database.
 *
 * For instance, `sql"select name from country".query[String]` defines a `Query0[String]`, which
 * is a one-column query that maps each returned row to a String.
 *
 * `.to[List]` is a convenience method that accumulates rows into a `List`, in this case yielding a
 * `ConnectionIO[List[String]]`. It works with any collection type that has a `CanBuildFrom`. Similar methods are:
 * - `.unique` which returns a single value, raising an exception if there is not exactly one row returned.
 * - `.option` which returns an `Option`, raising an exception if there is more than one row returned.
 * - `.nel` which returns a `NonEmptyList`, raising an exception if there are no rows returned.
 * See the Scaladoc for Query0 for more information on these and other methods.
 *
 * @param name selecting_data
 */
object SelectingDataSection extends AnyFlatSpec with Matchers with Section {

  /**
   * == Getting info about the countries ==
   *
   * To make simpler the code we built a method which prepares the database, makes the query and transacts
   * it all:
   *
   * {{{
   * def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
   *       transactor.use((createCountryTable *> insertCountries(countries) *> f).transact[IO])
   * }}}
   *
   * We can use the `unique` method if we expect the query to return only one row
   */
  def selectUniqueCountryName(res0: String) = {

    val countryName =
      transactorBlock(sql"select name from COUNTRY where code = 'ESP'".query[String].unique)
        .unsafeRunSync()

    countryName should be(res0)
  }

  /**
   * If we are not sure if the record exists, we can use the `option` method.
   */
  def selectOptionalCountryName(res0: Option[String]) = {

    val maybeCountryName =
      transactorBlock(sql"select name from country where code = 'ITA'".query[String].option)
        .unsafeRunSync()

    maybeCountryName should be(res0)
  }

  /**
   * When the query can return more than one row, we can use the `list` to accumulate the results
   * in a List.
   */
  def selectCountryNameList(res0: String) = {

    val countryNames =
      transactorBlock {
        sql"select name from country order by name".query[String].to[List]
      }.unsafeRunSync()

    countryNames.head should be(res0)
  }

  /**
   * This is ok, but there’s not much point reading all the results from the database when we only
   * want the first few rows.
   *
   * The difference here is that stream gives us an fs2 Stream[ConnectionIO, String] that emits
   * rows as they arrive from the database. By applying take(5) we instruct the stream to shut
   * everything down (and clean everything up) after five elements have been emitted. This is
   * much more efficient than pulling all 239 rows and then throwing most of them away.
   */
  def selectCountryNameListByUsingProcess(res0: Int) = {

    val countryNames =
      transactorBlock {
        sql"select name from country order by name".query[String].stream.take(3).compile.toList
      }.unsafeRunSync()

    countryNames.size should be(res0)
  }
}
