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

  test("Execute whole program") {
    evaluateProgram(Vector(99), 0) shouldBe Vector()
    evaluateProgram(Vector(3, 0, 4, 0, 99), 88) shouldBe Vector(88)
    evaluateProgram(Vector(3, 0, 4, 0, 4, 1, 99), 88) shouldBe Vector(0, 88)
    evaluateProgram(Vector(1002, 4, 3, 4, 33), 1) shouldBe Vector()
  }
}
