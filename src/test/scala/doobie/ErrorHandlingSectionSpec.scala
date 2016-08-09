package doobie

import org.scalacheck.Shapeless._
import org.scalaexercises.Test
import org.scalatest.Spec
import org.scalatest.prop.Checkers
import shapeless.HNil

import scalaz.\/

class ErrorHandlingSectionSpec extends Spec with Checkers {

  def `safe insert with attemptSome` = {
    check(
      Test.testSuccess(
        ErrorHandlingSection.safeInsertWithAttemptSome _,
        \/.left[String, Long]("Oops!") :: HNil
      )
    )
  }

  def `safe insert with attemptSomeSqlState` = {
    check(
      Test.testSuccess(
        ErrorHandlingSection.safeInsertWithAttemptSomeSqlState _,
        \/.left[String, Long]("John is already here!") :: HNil
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
