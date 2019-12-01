package day1

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import util.File
import util.Console

object TyrannyOfTheRocketEquation extends IOApp {

  type Fuel = Double
  type Mass = Double

  def fuelToLaunch(mass: Mass): Fuel = Math.floor(mass / 3) - 2

  def moreFuelNeeded(fuel: Fuel): Fuel = {
    val furtherFuel = Math.floor(fuel / 3) - 2
    if (furtherFuel <= 0) 0.0
    else furtherFuel + moreFuelNeeded(furtherFuel)
  }

  def moduleMasses: IO[Vector[Mass]] = for {
    lines <- File.readLines("/day1.txt")
    masses <- lines.traverse(line => IO(line.toDouble))
    } yield masses

  override def run(args: List[String]): IO[ExitCode] = for {
    masses <- moduleMasses
    fuelRequirements = masses.map(fuelToLaunch)
    totalFuel = fuelRequirements.sum
    _ <- Console.putStrLn(s"Total fuel needed for modules: $totalFuel")
    moduleFuelWithAdditionalFuelAdded = fuelRequirements.map(moreFuelNeeded)
    totalFuelRequirements = moduleFuelWithAdditionalFuelAdded.sum + totalFuel
    _ <- Console.putStrLn(s"Total fuel needed with additional fuel added: $totalFuelRequirements")
  } yield ExitCode.Success
}
