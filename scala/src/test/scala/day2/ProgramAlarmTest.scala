package day2

import day2.ProgramAlarm.Intcode
import org.scalatest.funsuite.AnyFunSuite

class ProgramAlarmTest extends AnyFunSuite {

  test("Add") {
    testProgram(Vector(1, 0, 0, 0, 99), Vector(2, 0, 0, 0, 99))
  }

  test("Multiply") {
    testProgram(Vector(2, 3, 0, 3, 99), Vector(2, 3, 0, 6, 99))
  }

  test("Stop early") {
    testProgram(
      Vector(1, 1, 1, 4, 99, 5, 6, 0, 99),
      Vector(30, 1, 1, 4, 2, 5, 6, 0, 99)
    )
  }

  test("Example program") {
    testProgram(
      Vector(1, 9, 10, 3, 2, 3, 11, 0, 99, 30, 40, 50),
      Vector(3500, 9, 10, 70, 2, 3, 11, 0, 99, 30, 40, 50)
    )

  }

  private def testProgram(program: Vector[Int], expected: Vector[Int]): Unit = {
    val result = ProgramAlarm.process(Intcode(0, program))
    assert(result.memory == expected, "Did not match expected state")
  }
}
