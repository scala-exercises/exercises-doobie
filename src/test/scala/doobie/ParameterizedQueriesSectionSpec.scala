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

class ParameterizedQueriesSectionSpec extends Spec with Checkers {

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
