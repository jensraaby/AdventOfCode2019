package day3

import cats.effect.{ExitCode, IO, IOApp}
import util.File
import util.Console
import cats.implicits._
import scala.annotation.tailrec

object CrossedWires extends IOApp {

  sealed trait Move
  final case class R(distance: Int) extends Move
  final case class L(distance: Int) extends Move
  final case class U(distance: Int) extends Move
  final case class D(distance: Int) extends Move

  final case class Wire(instructions: List[Move]) {
    def pathFromOrigin: List[(Int, Int)] =
      instructions.foldLeft(List(0 -> 0)) { (moves, move) =>
        val start = moves.last
        move match {
          case R(d) =>
            moves :++ pointsHorizontal(start, d)
          case L(d) =>
            moves :++ pointsHorizontal(start, -d)
          case U(d) =>
            moves :++ pointsVertical(start, d)
          case D(d) =>
            moves :++ pointsVertical(start, -d)
        }
      }
  }

  def pointsHorizontal(start: (Int, Int), distance: Int): List[(Int, Int)] = {
    for {
      x <- if (distance < 0) (1 to -distance).map(-_) else 1 to distance
    } yield (start._1 + x, start._2)
  }.toList

  def pointsVertical(start: (Int, Int), distance: Int): List[(Int, Int)] = {
    for {
      y <- if (distance < 0) (1 to -distance).map(-_) else 1 to distance
    } yield (start._1, start._2 + y)
  }.toList

  def parseWire(wire: String): Wire = {
    val movesAsStrings = wire.split(",")
    val moves = movesAsStrings.map { move =>
      move.splitAt(1) match {
        case ("R", steps) => R(steps.toInt)
        case ("L", steps) => L(steps.toInt)
        case ("U", steps) => U(steps.toInt)
        case ("D", steps) => D(steps.toInt)
      }
    }
    Wire(moves.toList)
  }

  def intersections(wire1: Wire, wire2: Wire): Set[(Int, Int)] = {
    val w1 = wire1.pathFromOrigin.toSet - (0 -> 0) // don't include origin
    val w2 = wire2.pathFromOrigin.toSet
    w1 intersect w2
  }

  def manhattanDistanceFromOrigin(point: (Int, Int)): Int =
    Math.abs(point._1) + Math.abs(point._2)

  def closestIntersectionDistance(wire1: Wire, wire2: Wire): Int = {
    val crossingPoints = intersections(wire1, wire2)
    val distances = crossingPoints.toList.map(manhattanDistanceFromOrigin)
    distances.min
  }

  def stepsToReachPoint(wire: Wire, point: (Int, Int)): IO[Int] = {

    @tailrec
    def distanceToPoint(remainingPath: List[(Int, Int)],
                        distanceSoFar: Int): Int =
      remainingPath match {
        case Nil => distanceSoFar
        case p :: rest =>
          if (p == point) distanceSoFar
          else distanceToPoint(rest, distanceSoFar + 1)
      }

    val path = wire.pathFromOrigin

    if (!path.contains(point))
      IO.raiseError(
        new RuntimeException(
          s"Invalid point passed: $point is not in path $path"
        )
      )
    else
      IO(distanceToPoint(path, 0))
  }

  def lowestSumOfStepsToIntersection(wire1: Wire, wire2: Wire): IO[Int] = {
    val crossingPoints = intersections(wire1, wire2)
    val sumOfSteps = crossingPoints.toList.traverse { crossing =>
      for {
        stepsW1 <- stepsToReachPoint(wire1, crossing)
        stepsW2 <- stepsToReachPoint(wire2, crossing)
      } yield stepsW1 + stepsW2
    }
    sumOfSteps.map(listOfSums => listOfSums.min)
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      input <- File.readLines("/day3.txt")
      w1 <- IO(parseWire(input.head))
      w2 <- IO(parseWire(input.last))
      result = closestIntersectionDistance(w1, w2)
      _ <- Console.putStrLn(
        s"Part 1: Closest Manhattan distance of an intersection is $result"
      )
      resultP2 <- lowestSumOfStepsToIntersection(w1, w2)
      _ <- Console.putStrLn(
        s"Part 2: Lowest sum of steps to an intersection: $resultP2"
      )
    } yield ExitCode.Success
}
