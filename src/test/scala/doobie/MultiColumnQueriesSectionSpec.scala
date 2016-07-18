package doobie

import doobie.Model.CountryInfo
import org.scalacheck.Shapeless._
import org.scalaexercises.Test
import org.scalatest.Spec
import org.scalatest.prop.Checkers
import shapeless.HNil

class MultiColumnQueriesSectionSpec extends Spec with Checkers {

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
