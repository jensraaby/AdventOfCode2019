package day4

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SecureContainerTest extends AnyFunSuite with Matchers {

  test("Num to digits") {
    SecureContainer.numToDigits(1) shouldBe List(1)
    SecureContainer.numToDigits(10) shouldBe List(1, 0)
    SecureContainer.numToDigits(123) shouldBe List(1, 2, 3)
  }

  test("Is increasing") {
    SecureContainer.isIncreasing(List(1, 2, 3)) shouldBe true
    SecureContainer.isIncreasing(List(1, 1, 1)) shouldBe true
    SecureContainer.isIncreasing(List(3, 1, 1)) shouldBe false
    SecureContainer.isIncreasing(List(1, 3, 4, 8, 0, 0)) shouldBe false
    SecureContainer.isIncreasing(List(1)) shouldBe true
    SecureContainer.isIncreasing(List(1, 2)) shouldBe true
    SecureContainer.isIncreasing(Nil) shouldBe true
  }

  test("Contains 2 adjacent same numbers") {
    SecureContainer.containsTwoSameAdjacent(List(1, 2)) shouldBe false
    SecureContainer.containsTwoSameAdjacent(List(1, 1)) shouldBe true
    SecureContainer.containsTwoSameAdjacent(List(1)) shouldBe false
    SecureContainer.containsTwoSameAdjacent(List(1, 2, 3, 4, 5)) shouldBe false
    SecureContainer.containsTwoSameAdjacent(List(1, 2, 3, 4, 4)) shouldBe true
  }

  test("Is good password") {
    SecureContainer.isGood(122) shouldBe false
    SecureContainer.isGood(1224567) shouldBe false

    SecureContainer.isGood(123456) shouldBe false
    SecureContainer.isGood(654321) shouldBe false
    SecureContainer.isGood(123446) shouldBe true
    SecureContainer.isGood(113446) shouldBe true
    SecureContainer.isGood(112233) shouldBe true

  }

  test("Find valid passwords") {
    SecureContainer.findValidPasswords(112233, 112234) should contain theSameElementsAs List(
      112233,
      112234
    )

    SecureContainer.findValidPasswords(123456, 123490) should contain theSameElementsAs List(
      123466,
      123477,
      123488
    )
  }
}
