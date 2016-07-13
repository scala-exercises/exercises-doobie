package doobie

import doobie.DoobieUtils._
import doobie.Model._
import doobie.imports._
import org.scalaexercises.definitions.Section
import org.scalatest.{FlatSpec, Matchers}

/**
  * We are gonna construct some programs that retrieve data from the database and stream it back,
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
  * For the exercises, the country table will contain:
  * {{{
  * code    name                      population    gnp
  * "DEU"  "Germany"                    82164700    2133367.00
  * "ESP"  "Spain"                      39441700     553223.00
  * "FRA"  "France",                    59225700    1424285.00
  * "GBR"  "United Kingdom"             59623400    1378330.00
  * "USA"  "United States of America"  278357000    8510700.00
  * }}}
  *
  * == How to select data ==
  *
  * As we commented in the previous section, the `sql` string interpolator allow us to create a
  * query to select data from the database.
  *
  * For instance, `sql"select name from country".query[String]` defines a `Query0[String]`, which
  * is a one-column query that maps each returned row to a String.
  *
  * Once we generate this query, we could use several convenience methods to stream the results:
  * - `.list`, which accumulates the results to a `List`, in this case yielding a
  * `ConnectionIO[List[String]]`.
  * - `.vector`, which accumulates to a `Vector`
  * - `.to[Coll]`, which accumulates to a type `Coll`, given an implicit `CanBuildFrom`. This works
  * with Scala standard library collections.
  * - `.accumulate[M[_]: MonadPlus]` which accumulates to a universally quantified monoid `M`.
  * This works with many scalaz collections, as well as standard library collections with
  * `MonadPlus` instances.
  * - `.unique` which returns a single value, raising an exception if there is not exactly
  * one row returned.
  * - `.option` which returns an Option, raising an exception if there is more than
  * one row returned.
  * - `.nel` which returns an `NonEmptyList`, raising an exception if there are no rows returned.
  * - See the Scaladoc for [[http://tpolecat.github.io/doc/doobie/0.3.0/api/index.html#doobie.util.query$$Query0 `Query0`]]
  * for more information on these and other methods.
  *
  * @param name selecting_data
  */
object SelectingDataSection extends FlatSpec with Matchers with Section {

  val initializeData = for {
    _ <- createCountryTable
    _ <- insertCountries(countries)
  } yield Unit

  val cleanupData = dropCountryTable

  /**
    * == Getting info about the countries ==
    *
    * We can use the `unique` method if we expect the query to return only one row
    */
  def selectUniqueCountryName(res0: String) = {

    val query = sql"select name from country where code = 'ESP'".query[String]

    val persistenceOps = for {
      _ <- initializeData
      countryNames <- query.unique
      _ <- cleanupData
    } yield countryNames

    val countryName: String = persistenceOps.transact(xa).run

    countryName should be(res0)
  }

  /**
    * If we are not sure if the record exists, we can use the `option` method.
    */
  def selectOptionalCountryName(res0: Option[String]) = {

    val query = sql"select name from country where code = 'ITA'".query[String]

    val persistenceOps = for {
      _ <- initializeData
      countryNames <- query.option
      _ <- cleanupData
    } yield countryNames

    val maybeCountryName: Option[String] = persistenceOps.transact(xa).run

    maybeCountryName should be(res0)
  }

  /**
    * When the query can return more than one row, we can use the `list` to accumulate the results
    * in a List.
    */
  def selectCountryNameList(res0: String) = {

    val query = sql"select name from country order by name".query[String]

    val persistenceOps = for {
      _ <- initializeData
      countryNames <- query.list
      _ <- cleanupData
    } yield countryNames

    val countryNames: List[String] = persistenceOps.transact(xa).run

    countryNames.head should be(res0)
  }

  /**
    * This is ok, but there’s not much point reading all the results from the database when we only
    * want the first few rows.
    *
    * A different approach could be to use the `process` that  gives us a
    * `scalaz.stream.Process[ConnectionIO, String]` which emits the results as they arrive from the
    * database. By applying a limit with `take` we instruct the process to shut everything down
    * (and clean everything up) after the required number of elements have been emitted. This is
    * much more efficient than pulling all the rows of the table and then throwing most of them away.
    */
  def selectCountryNameListByUsingProcess(res0: Int) = {

    val query = sql"select name from country order by name".query[String]

    val persistenceOps = for {
      _ <- initializeData
      countryNames <- query.process.take(3).list
      _ <- cleanupData
    } yield countryNames

    val countryNames: List[String] = persistenceOps.transact(xa).run

    countryNames.size should be(res0)
  }
}
