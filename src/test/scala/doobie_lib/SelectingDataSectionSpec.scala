/*
 *  scala-exercises - exercises-doobie
 *  Copyright (C) 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 */

package doobie_lib

import org.scalacheck.ScalacheckShapeless._
import org.scalaexercises.Test
import org.scalatest.BeforeAndAfterEach
import org.scalatest.refspec.RefSpec
import org.scalatestplus.scalacheck.Checkers
import shapeless.HNil

class SelectingDataSectionSpec extends RefSpec with Checkers with BeforeAndAfterEach {

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
