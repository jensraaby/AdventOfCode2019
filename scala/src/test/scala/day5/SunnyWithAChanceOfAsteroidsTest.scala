package day5

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SunnyWithAChanceOfAsteroidsTest extends AnyFunSuite with Matchers {
  import SunnyWithAChanceOfAsteroids._

  test("Opcode") {
    opCode(1) shouldBe Add
    opCode(101) shouldBe Add
    opCode(1001) shouldBe Add
    opCode(2) shouldBe Multiply
    opCode(1102) shouldBe Multiply
    opCode(3) shouldBe In
    opCode(4) shouldBe Out
    opCode(99) shouldBe Stop
  }

  test("Modes") {
    modes(1) shouldBe Vector(Position, Position, Position)
    modes(101) shouldBe Vector(Immediate, Position, Position)
    modes(1101) shouldBe Vector(Immediate, Immediate, Position)
    modes(2) shouldBe Vector(Position, Position, Position)
    modes(11102) shouldBe Vector(Immediate, Immediate, Immediate)
    modes(10002) shouldBe Vector(Position, Position, Immediate)
    modes(3) shouldBe Vector(Position, Position, Position)
    modes(103) shouldBe Vector(Immediate, Position, Position)
    modes(4) shouldBe Vector(Position, Position, Position)
    modes(99) shouldBe Vector(Position, Position, Position)
  }

  test("Load parameters from memory") {
    loadParameters(Vector(1, 2, 3))((Position, 0)) shouldBe 1
    loadParameters(Vector(1, 2, 3))((Immediate, 0)) shouldBe 0
    loadParameters(Vector(1, 2, 1))((Position, 2)) shouldBe 1
    loadParameters(Vector(0, 1, 2))((Immediate, 1)) shouldBe 1
  }

  test("Execute single step computer programs") {
    Computer(Vector(99)).execute(1) shouldBe None

    Computer(Vector(1, 0, 0, 1, 99)).execute(1) shouldBe
      Some(Computer(Vector(1, 2, 0, 1, 99), 4, Vector()))

    Computer(Vector(3, 0, 4, 0, 99)).execute(88) shouldBe
      Some(Computer(Vector(88, 0, 4, 0, 99), 2, Vector()))

    Computer(Vector(88, 0, 4, 0, 99), 2, Vector()).execute(1) shouldBe
      Some(Computer(Vector(88, 0, 4, 0, 99), 4, Vector(88)))

    Computer(Vector(1002, 4, 3, 4, 33)).execute(1) shouldBe
      Some(Computer(Vector(1002, 4, 3, 4, 99), 4, Vector()))

    Computer(Vector(1101, 100, -1, 4, 0)).execute(1) shouldBe
      Some(Computer(Vector(1101, 100, -1, 4, 99), 4, Vector()))
  }

  test("Execute whole program with part 1 instructions") {
    evaluateProgram(Vector(99), 0) shouldBe Vector()
    evaluateProgram(Vector(3, 0, 4, 0, 99), 88) shouldBe Vector(88)
    evaluateProgram(Vector(3, 0, 4, 0, 4, 1, 99), 88) shouldBe Vector(0, 88)
    evaluateProgram(Vector(1002, 4, 3, 4, 33), 1) shouldBe Vector()
  }

  test("Execute whole program with part 2 instructions") {
    val isInput8 = Vector(3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8)
    evaluateProgram(isInput8, 1) shouldBe Vector(0)
    evaluateProgram(isInput8, 8) shouldBe Vector(1)

    val isInputLessThan8 = Vector(3, 9, 7, 9, 10, 9, 4, 9, 99, -1, 8)
    evaluateProgram(isInputLessThan8, 99) shouldBe Vector(0)
    evaluateProgram(isInputLessThan8, 1) shouldBe Vector(1)

    val isEqualTo8ImmediateMode = Vector(3, 3, 1108, -1, 8, 3, 4, 3, 99)
    evaluateProgram(isEqualTo8ImmediateMode, 888) shouldBe Vector(0)
    evaluateProgram(isEqualTo8ImmediateMode, 8) shouldBe Vector(1)

    val isLessThan8ImmediateMode = Vector(3, 3, 1107, -1, 8, 3, 4, 3, 99)
    evaluateProgram(isLessThan8ImmediateMode, 888) shouldBe Vector(0)
    evaluateProgram(isLessThan8ImmediateMode, 7) shouldBe Vector(1)

    val jumpIfInput0 =
      Vector(3, 12, 6, 12, 15, 1, 13, 14, 13, 4, 13, 99, -1, 0, 1, 9)
    evaluateProgram(jumpIfInput0, 0) shouldBe Vector(0)
    evaluateProgram(jumpIfInput0, 1) shouldBe Vector(1)

    val jumpIfInput0ImmediateMode =
      Vector(3, 3, 1105, -1, 9, 1101, 0, 0, 12, 4, 12, 99, 1)
    evaluateProgram(jumpIfInput0ImmediateMode, 0) shouldBe Vector(0)
    evaluateProgram(jumpIfInput0ImmediateMode, 100) shouldBe Vector(1)

    val moreComplexTest = Vector(3, 21, 1008, 21, 8, 20, 1005, 20, 22, 107, 8,
      21, 20, 1006, 20, 31, 1106, 0, 36, 98, 0, 0, 1002, 21, 125, 20, 4, 20,
      1105, 1, 46, 104, 999, 1105, 1, 46, 1101, 1000, 1, 20, 4, 20, 1105, 1, 46,
      98, 99)
    evaluateProgram(moreComplexTest, 1) shouldBe Vector(999)
    evaluateProgram(moreComplexTest, 8) shouldBe Vector(1000)
    evaluateProgram(moreComplexTest, 9) shouldBe Vector(1001)

  }
}
