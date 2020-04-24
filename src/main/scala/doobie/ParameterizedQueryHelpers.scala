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

import cats.data._
import doobie._
import doobie.implicits._
import Model.Country

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
