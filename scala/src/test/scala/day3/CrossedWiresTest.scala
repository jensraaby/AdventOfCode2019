package day3

import day3.CrossedWires.{D, L, R, U, Wire}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CrossedWiresTest extends AnyFunSuite with Matchers {

  test("Parse wire") {
    CrossedWires.parseWire("R2,U2") shouldBe Wire(List(R(2), U(2)))
    CrossedWires.parseWire("L5,D10") shouldBe Wire(List(L(5), D(10)))
  }

  test("Horizontal points") {
    CrossedWires.pointsHorizontal((0, 0), 2) shouldBe List((1, 0), (2, 0))
    CrossedWires.pointsHorizontal((0, 0), -2) shouldBe List((-1, 0), (-2, 0))
    CrossedWires.pointsHorizontal((5, 2), -3) shouldBe List(
      (4, 2),
      (3, 2),
      (2, 2)
    )
  }

  test("Vertical points") {
    val p1 = (0, 0)
    CrossedWires.pointsVertical(p1, 2) shouldBe List((0, 1), (0, 2))
    CrossedWires.pointsVertical(p1, -2) shouldBe List((0, -1), (0, -2))
  }

  test("PathFromOrigin") {
    Wire(List(D(2), R(2), U(3), L(1))).pathFromOrigin shouldBe List(
      (0, 0),
      (0, -1),
      (0, -2),
      (1, -2),
      (2, -2),
      (2, -1),
      (2, 0),
      (2, 1),
      (1, 1)
    )
  }

  test("Intersections") {
    val inputWire1 = "R8,U5,L5,D3"
    val inputWire2 = "U7,R6,D4,L4"
    CrossedWires.intersections(
      Wire(List(R(8), U(5), L(5), D(3))),
      Wire(List(U(7), R(6), D(4), L(4)))
    ) shouldBe Set((6, 5), (3, 3))
  }

  test("Manhattan distance from origin") {
    CrossedWires.manhattanDistanceFromOrigin((1, 1)) shouldBe 2
    CrossedWires.manhattanDistanceFromOrigin((-5, 1)) shouldBe 6
    CrossedWires.manhattanDistanceFromOrigin((-5, -5)) shouldBe 10
  }

  test("Manhattan distance to closest intersection") {
    CrossedWires.closestIntersectionDistance(
      CrossedWires.parseWire("R8,U5,L5,D3"),
      CrossedWires.parseWire("U7,R6,D4,L4")
    ) shouldBe 6

    CrossedWires.closestIntersectionDistance(
      CrossedWires.parseWire("R75,D30,R83,U83,L12,D49,R71,U7,L72"),
      CrossedWires.parseWire("U62,R66,U55,R34,D71,R55,D58,R83")
    ) shouldBe 159

    CrossedWires.closestIntersectionDistance(
      CrossedWires.parseWire("R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51"),
      CrossedWires.parseWire("U98,R91,D20,R16,D67,R40,U7,R15,U6,R7")
    ) shouldBe 135
  }

  test("Steps to reach a point") {
    CrossedWires
      .stepsToReachPoint(CrossedWires.parseWire("R8,U5,L5,D3"), (3, 3))
      .unsafeRunSync() shouldBe 20

    CrossedWires
      .stepsToReachPoint(CrossedWires.parseWire("U7,R6,D4,L4"), (3, 3))
      .unsafeRunSync() shouldBe 20
  }

  test("Steps to reach intersection with fewest moves") {
    CrossedWires
      .lowestSumOfStepsToIntersection(
        CrossedWires.parseWire("R8,U5,L5,D3"),
        CrossedWires.parseWire("U7,R6,D4,L4")
      )
      .unsafeRunSync() shouldBe 30

    CrossedWires
      .lowestSumOfStepsToIntersection(
        CrossedWires.parseWire("R75,D30,R83,U83,L12,D49,R71,U7,L72"),
        CrossedWires.parseWire("U62,R66,U55,R34,D71,R55,D58,R83")
      )
      .unsafeRunSync() shouldBe 610

    CrossedWires
      .lowestSumOfStepsToIntersection(
        CrossedWires.parseWire("R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51"),
        CrossedWires.parseWire("U98,R91,D20,R16,D67,R40,U7,R15,U6,R7")
      )
      .unsafeRunSync() shouldBe 410
  }
}
