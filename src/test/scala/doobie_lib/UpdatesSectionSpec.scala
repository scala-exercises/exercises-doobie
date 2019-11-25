/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package doobie_lib

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
