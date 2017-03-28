/*
 * scala-exercises - exercises-doobie
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package doobie

import doobie.DoobieUtils.CountryTable._
import doobie.Model._
import doobie.ParameterizedQueryHelpers._
import doobie.imports._
import org.scalaexercises.definitions.Section
import org.scalatest.{FlatSpec, Matchers}

import scalaz.NonEmptyList

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
 * @param name parameterized_queries
 */
object ParameterizedQueriesSection extends FlatSpec with Matchers with Section {

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

    val countriesName = biggerThan(75000000).list
      .transact(xa)
      .run
      .map(_.name)

    countriesName should be(List(res0, res1))
  }

  /**
   * So what’s going on? It looks like we’re just dropping a string literal into our SQL string,
   * but actually we’re constructing a proper parameterized PreparedStatement, and the minProp
   * value is ultimately set via a call to setInt
   *
   * '''doobie''' allows you to interpolate values of any type with a `Atom` instance, which
   * includes:
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

    val countriesName = populationIn(25000000 to 75000000).list
      .transact(xa)
      .run
      .map(_.name)

    countriesName should be(List(res0, res1, res2))
  }

  /**
   * == Dealing with IN Clauses ==
   *
   * A common irritant when dealing with SQL literals is the desire to inline a sequence of
   * arguments into an IN clause, but SQL does not support this notion (nor does JDBC do anything
   * to assist). So as of version 0.2.3 doobie provides support in the form of some slightly
   * inconvenient machinery.
   * {{{
   *   def populationIn(range: Range, codes: NonEmptyList[String]) = {
   *     implicit val codesParam = Param.many(codes)
   *     sql"""
   *       select code, name, population, gnp
   *       from country
   *       where population > ${range.min}
   *       and   population < ${range.max}
   *       and   code in (${codes : codes.type})
   *     """.query[Country]
   *   }
   * }}}
   *
   * There are a few things to notice here:
   *  - The `IN` clause must be non-empty, so `codes` is a `NonEmptyList`.
   *  - We must derive a `Param` instance for the singleton type of `codes`, which we do via
   *  `Param.many`. This derivation is legal for any `F[A]` given `Foldable1[F]` and `Atom[A]`. You
   *  can have any number of `IN` arguments but each must have its own derived `Param` instance.
   *  - When interpolating `codes` we must explicitly ascribe its singleton type `codes.type`.
   */
  def dealingWithInClause(res0: String, res1: String) = {

    val countriesName = populationIn(25000000 to 75000000, NonEmptyList("ESP", "USA", "FRA")).list
      .transact(xa)
      .run
      .map(_.name)

    countriesName should be(List(res0, res1))
  }
}
