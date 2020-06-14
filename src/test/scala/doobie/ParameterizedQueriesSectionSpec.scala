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

import org.scalacheck.ScalacheckShapeless._
import org.scalaexercises.Test
import org.scalatest.refspec.RefSpec
import org.scalatestplus.scalacheck.Checkers
import shapeless.HNil

class ParameterizedQueriesSectionSpec extends RefSpec with Checkers {

  def `adding a parameter` = {
    check(
      Test.testSuccess(
        ParameterizedQueriesSection.addingAParameter _,
        "Germany" :: "United States of America" :: HNil
      )
    )
  }

  def `adding multiple parameters` = {
    check(
      Test.testSuccess(
        ParameterizedQueriesSection.addingMultipleParameters _,
        "Spain" :: "France" :: "United Kingdom" :: HNil
      )
    )
  }

  def `dealing with IN clause` = {
    check(
      Test.testSuccess(
        ParameterizedQueriesSection.dealingWithInClause _,
        "Spain" :: "France" :: HNil
      )
    )
  }
}
