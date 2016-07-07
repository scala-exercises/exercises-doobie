package exercises

import cats.data.Xor
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalatest.exceptions._
import shapeless._
import shapeless.ops.function._

object Test {

  def testSuccess[F, R, L <: HList](method: F, answer: L)(
    implicit
    A: Arbitrary[L],
    fntop: FnToProduct.Aux[F, L ⇒ R]
  ): Prop = {
    val rightGen = genRightAnswer(answer)
    val rightProp = forAll(rightGen)({ p ⇒

      val result = Xor.catchOnly[GeneratorDrivenPropertyCheckFailedException]({
        fntop(method)(p)
      })
      result match {
        case Xor.Left(exc) ⇒ exc.cause match {
          case Some(originalException) ⇒ throw originalException
          case _ ⇒ false
        }
        case _ ⇒ true
      }
    })

    val wrongGen = genWrongAnswer(answer)
    val wrongProp = forAll(wrongGen)({ p ⇒
      Xor.catchNonFatal({
        fntop(method)(p)
      }).isLeft
    })

    Prop.all(rightProp, wrongProp)
  }

  def genRightAnswer[L <: HList](answer: L): Gen[L] = {
    Gen.const(answer)
  }

  def genWrongAnswer[L <: HList](l: L)(
    implicit
    A: Arbitrary[L]
  ): Gen[L] = {
    A.arbitrary.suchThat(_ != l)
  }
}
