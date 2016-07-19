package doobie

import doobie.Model.Country
import doobie.imports._
import scalaz.NonEmptyList

object ParameterizedQueryHelpers {

  def biggerThan(minPop: Int) =
    sql"select code, name, population, gnp from country where population > $minPop order by population asc"
      .query[Country]

  def populationIn(range: Range) =
    sql"select code, name, population, gnp from country where population > ${range.min} and population < ${range.max} order by population asc"
      .query[Country]

  def populationIn(range: Range, codes: NonEmptyList[String]) = {
    implicit val codesParam = Param.many(codes)
    sql"select code, name, population, gnp from country where population > ${range.min} and population < ${range.max} and code in (${codes: codes.type}) order by population asc"
      .query[Country]
  }
}
