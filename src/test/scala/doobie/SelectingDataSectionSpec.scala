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

class SelectingDataSectionSpec extends Spec with Checkers {

  def `select country name list` = {
    check(
      Test.testSuccess(
        SelectingDataSection.selectCountryNameList _,
        "France" :: HNil
      )
    )
  }

  def `select country name list by using process` = {
    check(
      Test.testSuccess(
        SelectingDataSection.selectCountryNameListByUsingProcess _,
        3 :: HNil
      )
    )
  }

  def `select optional country name` = {
    val value: Option[String] = None
    check(
      Test.testSuccess(
        SelectingDataSection.selectOptionalCountryName _,
        value :: HNil
      )
    )
  }

  def `select unique country name` = {
    check(
      Test.testSuccess(
        SelectingDataSection.selectUniqueCountryName _,
        "Spain" :: HNil
      )
    )
  }

}
