package day10

import cats.effect.{ExitCode, IO, IOApp}
import util.File
import util.Console

import scala.collection.mutable

object MonitoringStation extends IOApp {

  case class Asteroid(x: Int, y: Int) {
    def distanceTo(other: Asteroid): Double = {
      val xDiff = Math.abs(x - other.x)
      val yDiff = Math.abs(y - other.y)
      Math.hypot(xDiff, yDiff)
    }

    def angleTo(other: Asteroid): Double = {
      val angle = -Math.atan2(other.y - y, other.x - x)
      if (angle >= -Math.PI / 2) angle else angle + (2 * Math.PI)
    }

    def visibleFrom(asteroids: Set[Asteroid]): Int = {
      val asteroidsWithoutCurrent = asteroids - this
      val angles = for {
        asteroid <- asteroidsWithoutCurrent
      } yield angleTo(asteroid)
      angles.size
    }
  }

  def bestAsteroidToUseForMonitoring(asteroids: Set[Asteroid]): Asteroid = {
    asteroids.maxBy(_.visibleFrom(asteroids))
  }

  def parseGrid(inputLines: Vector[String]): IO[mutable.HashSet[Asteroid]] =
    IO {
      val xSize = inputLines.head.length
      if (!inputLines.map(_.length).forall(_ == xSize)) {
        throw new RuntimeException("Invalid grid - not all lines same size")
      }

      val filledCells = mutable.HashSet[Asteroid]()
      inputLines.zipWithIndex.foreach {
        case (line, y) =>
          line.toCharArray.zipWithIndex.foreach {
            case (char, x) =>
              if (char == '#') {
                filledCells.add(Asteroid(x, y))
              }
          }
      }
      filledCells
    }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      testLines <- IO(Vector(".#..#", ".....", "#####", "....#", "...##"))
      grid <- parseGrid(inputLines = testLines)
      _ <- Console.putStrLn(
        s"Best asteroid in small test: ${bestAsteroidToUseForMonitoring(grid.toSet)}"
      )
      bigTestLines <- File.readLines("/day10-test210.txt")
      bigGrid <- parseGrid(bigTestLines)
      _ <- Console.putStrLn(
        s"Best asteroid in big test: ${bestAsteroidToUseForMonitoring(bigGrid.toSet)}"
      )

      input <- File.readLines("/day10.txt")
      testGrid <- parseGrid(input)
      bestAsteroid = bestAsteroidToUseForMonitoring(testGrid.toSet)
      _ <- Console.putStrLn(s"Best asteroid in part 1: $bestAsteroid")
      _ <- Console.putStrLn(
        s"Number of visible asteroids from $bestAsteroid: ${bestAsteroid
          .visibleFrom(testGrid.toSet)}"
      )
    } yield ExitCode.Success
}
