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
      // flipped x and y so angle is from y axis not x axis
      -Math.atan2(other.x - x, other.y - y)
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

  def destructionOrder(monitoringStationAsteroid: Asteroid,
                       asteroids: Set[Asteroid]) = {
    val asteroidsWithoutMonitoringStation = asteroids - monitoringStationAsteroid
    val toDestroy = mutable.HashMap[Double, List[Asteroid]]()

    asteroidsWithoutMonitoringStation.foreach { asteroid =>
      toDestroy.updateWith(monitoringStationAsteroid.angleTo(asteroid)) {
        case Some(lst) => Some(asteroid :: lst)
        case None      => Some(List(asteroid))
      }
    }

    toDestroy.keys.foreach { angle =>
      toDestroy.updateWith(angle) {
        case Some(lst) =>
          Some(lst.sortBy(_.distanceTo(monitoringStationAsteroid)))
        case None => None
      }
    }

    val destroyed = mutable.ListBuffer[Asteroid]()
    toDestroy.keys.toList.sorted.foreach { angle =>
      val closestAsteroid :: rest = toDestroy(angle)
      toDestroy.update(angle, rest)
      destroyed.append(closestAsteroid)
    }
    destroyed
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
      twoHundredthAsteroidToDestroy = destructionOrder(
        bestAsteroidToUseForMonitoring(testGrid.toSet),
        testGrid.toSet
      )(199)
      _ <- Console.putStrLn(
        s"200th asteroid to destroy: ${twoHundredthAsteroidToDestroy}"
      )
      _ <- Console.putStrLn(
        s"Part 2 solution: ${twoHundredthAsteroidToDestroy.x * 100 + twoHundredthAsteroidToDestroy.y}"
      )
    } yield ExitCode.Success
}
