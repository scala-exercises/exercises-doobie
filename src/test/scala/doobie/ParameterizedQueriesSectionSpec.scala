/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
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
