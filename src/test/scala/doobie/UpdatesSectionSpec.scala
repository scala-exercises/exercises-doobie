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

class UpdatesSectionSpec extends RefSpec with Checkers {

  def `insert one row` = {
    check(
      Test.testSuccess(
        UpdatesSection.insertOneRow _,
        1 :: HNil
      )
    )
  }

  def `insert several rows by using for-comprehension` = {
    check(
      Test.testSuccess(
        UpdatesSection.insertSeveralRowsWithForComprehension _,
        3 :: HNil
      )
    )
  }

  def `insert several rows by using applicative functor` = {
    check(
      Test.testSuccess(
        UpdatesSection.insertSeveralRowsWithApplicativeFunctor _,
        2 :: HNil
      )
    )
  }

  def `insert several rows by using traverse` = {
    check(
      Test.testSuccess(
        UpdatesSection.insertSeveralRowsWithTraverse _,
        4 :: HNil
      )
    )
  }

  def `update an existing row` = {
    check(
      Test.testSuccess(
        UpdatesSection.updateExistingRow _,
        1 :: 1 :: 15 :: HNil
      )
    )
  }

  def `retrieve info` = {
    check(
      Test.testSuccess(
        UpdatesSection.retrieveInfo _,
        "Ramone" :: 42 :: HNil
      )
    )
  }

  def `batch updates` = {
    check(
      Test.testSuccess(
        UpdatesSection.batchUpdates _,
        2 :: HNil
      )
    )
  }
}
