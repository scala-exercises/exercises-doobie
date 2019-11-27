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
import shapeless._

class ErrorHandlingSectionSpec extends RefSpec with Checkers {

  def `safe insert with attemptSome` = {
    check(
      Test.testSuccess(
        ErrorHandlingSection.safeInsertWithAttemptSome _,
        (Left("Oops!"): Either[String, Long]) :: HNil
      )
    )
  }

  def `safe insert with attemptSomeSqlState` = {
    check(
      Test.testSuccess(
        ErrorHandlingSection.safeInsertWithAttemptSomeSqlState _,
        (Left("John is already here!"): Either[String, Long]) :: HNil
      )
    )
  }

  def `safe insert with exceptSqlState` = {
    check(
      Test.testSuccess(
        ErrorHandlingSection.safeInsertWithExceptSqlState _,
        "John_20" :: Option(20) :: HNil
      )
    )
  }
}
