package doobie

import doobie.DoobieUtils.CountryTable._
import doobie.Model._
import doobie.imports._
import org.scalaexercises.definitions.Section
import org.scalatest.{FlatSpec, Matchers}
import shapeless.{::, HNil}

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
  * @param name multi_column_queries
  */
object MultiColumnQueriesSection extends FlatSpec with Matchers with Section {

  /**
    * We can select multiple columns and map them to a tuple. The `gnp` column in our table is
    * nullable so we’ll select that one into an `Option[Double]`.
    */
  def selectMultipleColumnsUsingTuple(res0: Option[Double]) = {

    val (name, population, gnp) =
      sql"select name, population, gnp from country where code = 'ESP'"
        .query[(String, Int, Option[Double])]
        .unique
        .transact(xa)
        .unsafePerformIO()

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
      sql"select name, population, gnp from country where code = 'FRA'"
        .query[CountryHListType]
        .unique
        .transact(xa)
        .unsafePerformIO()

    hlist.head should be(res0)
  }

  /**
    * And with a shapeless record:
    */
  def selectMultipleColumnsUsingRecord(res0: Int) = {
    import shapeless.record._ 
   
    type Rec = Record.`'name -> String, 'pop -> Int, 'gnp -> Option[Double]`.T

    val record: Rec =
      sql"select name, population, gnp from country where code = 'USA'"
        .query[Rec]
        .unique
        .transact(xa)
        .unsafePerformIO()

    record('pop) should be(res0)
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
      sql"select code, name, population, gnp from country where name = 'United Kingdom'"
        .query[Country]
        .unique
        .transact(xa)
        .unsafePerformIO()

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
      sql"select code, name, population, gnp from country where code = 'ESP'"
        .query[(Code, CountryInfo)]
        .unique
        .transact(xa)
        .unsafePerformIO()

    country.name should be(res0)
  }

  /**
    * And just for fun, since the `Code` values are constructed from the primary key, let’s turn the
    * results into a `Map`. Trivial but useful.
    */
  def selectMultipleColumnsUsingMap(res0: String, res1: Option[CountryInfo]) = {

    val notFoundCountry = CountryInfo("Not Found", 0, None)

    val countriesMap: Map[Code, CountryInfo] =
      sql"select code, name, population, gnp from country"
        .query[(Code, CountryInfo)]
        .list
        .transact(xa)
        .unsafePerformIO()
        .toMap

    countriesMap.getOrElse(Code("DEU"), notFoundCountry).name should be(res0)
    countriesMap.get(Code("ITA")) should be(res1)
  }
}
