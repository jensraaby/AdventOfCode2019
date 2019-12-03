package day3

import cats.effect.{ExitCode, IO, IOApp}
import util.File
import util.Console

object CrossedWires extends IOApp {

  sealed trait Move
  final case class R(distance: Int) extends Move
  final case class L(distance: Int) extends Move
  final case class U(distance: Int) extends Move
  final case class D(distance: Int) extends Move

  final case class Wire(instructions: Vector[Move]) {
    def pathFromOrigin: Vector[(Int, Int)] =
      instructions.foldLeft(Vector(0 -> 0)) { (moves, move) =>
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

  def pointsHorizontal(start: (Int, Int), distance: Int): Vector[(Int, Int)] = {
    for {
      x <- if (distance < 0) (1 to -distance).map(-_) else 1 to distance
    } yield (start._1 + x, start._2)
  }.toVector

  def pointsVertical(start: (Int, Int), distance: Int): Vector[(Int, Int)] = {
    for {
      y <- if (distance < 0) (1 to -distance).map(-_) else 1 to distance
    } yield (start._1, start._2 + y)
  }.toVector

  def parseWire(wire: String): Wire = {
    val movesAsStrings = wire.split(",")
    val moves = movesAsStrings.map { move =>
      move.toCharArray match {
        case Array('R', tail @ _*) => R(tail.mkString("").toInt)
        case Array('L', tail @ _*) => L(tail.mkString("").toInt)
        case Array('U', tail @ _*) => U(tail.mkString("").toInt)
        case Array('D', tail @ _*) => D(tail.mkString("").toInt)
      }
    }
    Wire(moves.toVector)
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
    val distances = crossingPoints.toVector.map(manhattanDistanceFromOrigin)
    distances.min
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      input <- File.readLines("/day3.txt")
      w1 <- IO(parseWire(input.head))
      w2 <- IO(parseWire(input.last))
      result = closestIntersectionDistance(w1, w2)
      _ <- Console.putStrLn(
        s"Closest Manhattan distance of an intersection is $result"
      )
    } yield ExitCode.Success
}
