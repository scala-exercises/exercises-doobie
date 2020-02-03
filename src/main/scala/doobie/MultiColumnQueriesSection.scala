/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package doobielib

import doobie.implicits._
import DoobieUtils.CountryTable._
import Model._
import org.scalaexercises.definitions.Section
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import shapeless._
import shapeless.record._

/**
 * So far, we have constructed queries that return single-column results. These results were mapped
 * to Scala types. But how can we deal with multi-column queries?
 *
 * In this section, we'll see what alternatives '''doobie''' offers us to work with multi-column
 * queries.
 *
 * As in previous sections, we'll keep working with the 'country' table:
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
 *
 * {{{
 * def transactorBlock[A](f: => ConnectionIO[A]): IO[A] =
 *    transactor.use((createCountryTable *> insertCountries(countries) *> f).transact[IO])
 * }}}
 *
 * @param name multi_column_queries
 */
object MultiColumnQueriesSection extends AnyFlatSpec with Matchers with Section {

  /**
   * We can select multiple columns and map them to a tuple. The `gnp` column in our table is
   * nullable so we’ll select that one into an `Option[Double]`.
   */
  def selectMultipleColumnsUsingTuple(res0: Option[Double]) = {

    val (name, population, gnp) =
      transactorBlock {
        sql"select name, population, gnp from country where code = 'ESP'"
          .query[(String, Int, Option[Double])]
          .unique
      }.unsafeRunSync()

    gnp should be(res0)
  }

  /**
   * doobie automatically supports row mappings for atomic column types, as well as options,
   * tuples, `HList`s, shapeless records, and case classes thereof. So let’s try the same query
   * with an `HList`
   */
  def selectMultipleColumnsUsingHList(res0: String) = {

    type CountryHListType = String :: Int :: Option[Double] :: HNil

    val hlist: CountryHListType =
      transactorBlock {
        sql"select name, population, gnp from country where code = 'FRA'"
          .query[CountryHListType]
          .unique
      }.unsafeRunSync()

    hlist.head should be(res0)
  }

  /**
   * And with a shapeless record:
   */
  def selectMultipleColumnsUsingRecord(res0: Int) = {

    type Rec = Record.`'name -> String, 'pop -> Int, 'gnp -> Option[Double]`.T

    val record: Rec =
      transactorBlock {
        sql"select name, population, gnp from country where code = 'USA'"
          .query[Rec]
          .unique
      }.unsafeRunSync()

    record(Symbol("pop")) should be(res0)
  }

  /**
   * And again, mapping rows to a case class.
   *
   * {{{
   * case class Country(code: String, name: String, population: Long, gnp: Option[Double])
   * }}}
   */
  def selectMultipleColumnsUsingCaseClass(res0: String) = {

    val country =
      transactorBlock {
        sql"select code, name, population, gnp from country where name = 'United Kingdom'"
          .query[Country]
          .unique
      }.unsafeRunSync()

    country.code should be(res0)
  }

  /**
   * You can also nest case classes, `HList`s, shapeless records, and/or tuples arbitrarily as long
   * as the eventual members are of supported columns types. For instance, here we map the same set
   * of columns to a tuple of two case classes:
   *
   * {{{
   * case class Code(code: String)
   * case class CountryInfo(name: String, pop: Int, gnp: Option[Double])
   * }}}
   */
  def selectMultipleColumnsUsingNestedCaseClass(res0: String) = {

    val (code, country) =
      transactorBlock {
        sql"select code, name, population, gnp from country where code = 'ESP'"
          .query[(Code, CountryInfo)]
          .unique
      }.unsafeRunSync()

    country.name should be(res0)
  }

  /**
   * And just for fun, since the `Code` values are constructed from the primary key, let’s turn the
   * results into a `Map`. Trivial but useful.
   */
  def selectMultipleColumnsUsingMap(res0: String, res1: Option[CountryInfo]) = {

    val notFoundCountry = CountryInfo("Not Found", 0, None)

    val countriesMap: Map[Code, CountryInfo] =
      transactorBlock {
        sql"select code, name, population, gnp from country"
          .query[(Code, CountryInfo)]
          .to[List]
      }.unsafeRunSync().toMap

    countriesMap.getOrElse(Code("DEU"), notFoundCountry).name should be(res0)
    countriesMap.get(Code("ITA")) should be(res1)
  }
}
