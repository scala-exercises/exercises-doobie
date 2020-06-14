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

object Model {

  case class Country(code: String, name: String, population: Long, gnp: Option[Double])

  case class Code(code: String)

  case class CountryInfo(name: String, pop: Int, gnp: Option[Double])

  val countries = List(
    Country("DEU", "Germany", 82164700, Option(2133367.00)),
    Country("ESP", "Spain", 39441700, None),
    Country("FRA", "France", 59225700, Option(1424285.00)),
    Country("GBR", "United Kingdom", 59623400, Option(1378330.00)),
    Country("USA", "United States of America", 278357000, Option(8510700.00))
  )
}
