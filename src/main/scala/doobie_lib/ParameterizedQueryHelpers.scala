/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package doobie_lib

import cats.data._
import doobie._
import doobie.implicits._
import doobie_lib.Model.Country

object ParameterizedQueryHelpers {

  def biggerThan(minPop: Int) =
    sql"select code, name, population, gnp from country where population > $minPop order by population asc"
      .query[Country]

  def populationIn(range: Range) =
    sql"select code, name, population, gnp from country where population > ${range.min} and population < ${range.max} order by population asc"
      .query[Country]

  def populationIn(range: Range, codes: NonEmptyList[String]) = {
    val q = fr"""
    select code, name, population, gnp
    from country
    where population > ${range.min}
    and   population < ${range.max}
    and   """ ++ Fragments.in(fr"code", codes) // code IN (...)
    q.query[Country]
  }
}
