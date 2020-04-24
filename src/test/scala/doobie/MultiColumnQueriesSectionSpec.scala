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

import org.scalacheck.ScalacheckShapeless._
import Model.CountryInfo
import org.scalacheck.{Arbitrary, Gen}
import org.scalaexercises.Test
import org.scalatest.refspec.RefSpec
import org.scalatestplus.scalacheck.Checkers
import shapeless.HNil

class MultiColumnQueriesSectionSpec extends RefSpec with Checkers {

  implicit val countryInfoArbitrary: Arbitrary[CountryInfo] = Arbitrary {
    for {
      name <- Gen.identifier
      pop  <- Gen.posNum[Int]
      gnp  <- Gen.option(Gen.posNum[Double])
    } yield CountryInfo(name, pop, gnp)
  }

  def `select multiple columns using tuple` = {
    val validResult: Option[Double] = None
    check(
      Test.testSuccess(
        MultiColumnQueriesSection.selectMultipleColumnsUsingTuple _,
        validResult :: HNil
      )
    )
  }

  def `select multiple columns using HList` = {
    check(
      Test.testSuccess(
        MultiColumnQueriesSection.selectMultipleColumnsUsingHList _,
        "France" :: HNil
      )
    )
  }

  def `select multiple columns using Record` = {
    check(
      Test.testSuccess(
        MultiColumnQueriesSection.selectMultipleColumnsUsingRecord _,
        278357000 :: HNil
      )
    )
  }

  def `select multiple columns using case class` = {
    check(
      Test.testSuccess(
        MultiColumnQueriesSection.selectMultipleColumnsUsingCaseClass _,
        "GBR" :: HNil
      )
    )
  }

  def `select multiple columns using nested case class` = {
    check(
      Test.testSuccess(
        MultiColumnQueriesSection.selectMultipleColumnsUsingNestedCaseClass _,
        "Spain" :: HNil
      )
    )
  }

  def `select multiple columns using map` = {
    val validResult: Option[CountryInfo] = None
    check(
      Test.testSuccess(
        MultiColumnQueriesSection.selectMultipleColumnsUsingMap _,
        "Germany" :: validResult :: HNil
      )
    )
  }
}
