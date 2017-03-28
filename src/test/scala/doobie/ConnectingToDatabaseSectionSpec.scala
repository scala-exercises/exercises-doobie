/*
 * scala-exercises - exercises-doobie
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package doobie

import org.scalacheck.Shapeless._
import org.scalaexercises.Test
import org.scalatest.Spec
import org.scalatest.prop.Checkers
import shapeless.HNil

class ConnectingToDatabaseSectionSpec extends Spec with Checkers {

  def `compute constant value` = {
    check(
      Test.testSuccess(
        ConnectingToDatabaseSection.constantValue _,
        42 :: HNil
      )
    )
  }

  def `compute constant value from database` = {
    check(
      Test.testSuccess(
        ConnectingToDatabaseSection.constantValueFromDatabase _,
        42 :: HNil
      )
    )
  }

  def `combine two small programs` = {
    check(
      Test.testSuccess(
        ConnectingToDatabaseSection.combineTwoPrograms _,
        (42, 25) :: HNil
      )
    )
  }

  def `combine two small programs with applicative` = {
    check(
      Test.testSuccess(
        ConnectingToDatabaseSection.combineTwoProgramsWithApplicative _,
        67 :: HNil
      )
    )
  }

}
