package day2

import cats.effect.IO
import cats.implicits._
import cats.effect.syntax.all._
import util.File
import util.Console

import scala.annotation.tailrec

object ProgramAlarm extends App {
  case class Intcode(index: Int, program: Vector[Int])

  sealed trait Operation
  case class Add(position1: Int, position2: Int, resultPosition: Int) extends Operation
  case class Mul(position1: Int, position2: Int, resultPosition: Int) extends Operation
  case object Stop extends Operation


  def step(operation: Operation, program: Intcode): Intcode = {
    operation match {
      case Add(p1, p2, p3) =>
        val result = program.program(p1) + program.program(p2)
        program.copy(
          index = program.index + 4,
          program = program.program.updated(p3, result))
      case Mul(p1, p2, p3) =>
        val result = program.program(p1) * program.program(p2)
        program.copy(
          index = program.index + 4,
          program = program.program.updated(p3, result))
      case Stop => program
    }
  }

  @tailrec
  def process(intcode: Intcode): Intcode = {
    val operation = intcode.program(intcode.index) match {
      case 1 => Add(intcode.program(intcode.index+1), intcode.program(intcode.index+2), intcode.program(intcode.index+3))
      case 2 => Mul(intcode.program(intcode.index+1), intcode.program(intcode.index+2), intcode.program(intcode.index+3))
      case 99 => Stop
      case other => throw new RuntimeException(s"could not interpret operation: $other")
    }

    if (operation == Stop) {
      intcode
    } else {
      val nextState = step(operation, intcode)
      process(nextState)
    }
  }


  def testProgram(program: Vector[Int], expected: Vector[Int]): Unit = {
    val result = process(Intcode(0, program))
    println(s"TestProgram: $program")
    println(s"Result: ${result.program}")
    println(s"Expected: $expected")
    assert(result.program == expected, "Did not match expected state")
  }

  testProgram(Vector(1,9,10,3,2,3,11,0,99,30, 40,50), Vector(3500,9,10,70, 2, 3, 11, 0, 99, 30, 40, 50))
  testProgram(Vector(1,0,0,0,99), Vector(2,0,0,0,99))
  testProgram(Vector(2,3,0,3,99), Vector(2,3,0,6,99))
  testProgram(Vector(1,1,1,4,99,5,6,0,99), Vector(30,1,1,4,2,5,6,0,99))


  val solvePuzzle = for {
    f <- File.readLines("/day2.txt")
    allLinesConcatenated = f.mkString(",")
    program <- allLinesConcatenated.split(",").toVector.traverse(str => IO(str.toInt))
    initialStateBeforeComputerCaughtFire = Intcode(0, program.updated(1, 12).updated(2, 2))
    result = process(initialStateBeforeComputerCaughtFire)
    _ <- Console.putStrLn(s"Value at position 0 after execution: ${result.program(0)}")
  } yield result.program

  solvePuzzle.unsafeRunSync()
}
