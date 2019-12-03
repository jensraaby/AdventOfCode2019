package day2

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import util.{Console, File}

import scala.annotation.tailrec

object ProgramAlarm extends IOApp {
  // Structure to represent memory and current function pointer
  case class Intcode(functionPointer: Int, memory: Vector[Int])

  // Operations represent instructions as a simple data structure
  sealed trait Operation
  case class Add(position1: Int, position2: Int, resultPosition: Int)
      extends Operation
  case class Mul(position1: Int, position2: Int, resultPosition: Int)
      extends Operation
  case object Stop extends Operation

  // Step evaluates an operation on the current state of the program, returning the new state (with updated function pointer)
  def step(operation: Operation, program: Intcode): Intcode = {
    operation match {
      case Add(p1, p2, p3) =>
        val result = program.memory(p1) + program.memory(p2)
        program.copy(
          functionPointer = program.functionPointer + 4,
          memory = program.memory.updated(p3, result)
        )
      case Mul(p1, p2, p3) =>
        val result = program.memory(p1) * program.memory(p2)
        program.copy(
          functionPointer = program.functionPointer + 4,
          memory = program.memory.updated(p3, result)
        )
      case Stop => program
    }
  }

  // Process runs through a program recursively until a stop operation is reached
  @tailrec
  def process(intcode: Intcode): Intcode = {
    val operation = intcode.memory(intcode.functionPointer) match {
      case 1 =>
        Add(
          intcode.memory(intcode.functionPointer + 1),
          intcode.memory(intcode.functionPointer + 2),
          intcode.memory(intcode.functionPointer + 3)
        )
      case 2 =>
        Mul(
          intcode.memory(intcode.functionPointer + 1),
          intcode.memory(intcode.functionPointer + 2),
          intcode.memory(intcode.functionPointer + 3)
        )
      case 99 => Stop
      case other =>
        throw new RuntimeException(s"Could not interpret operation: $other")
    }

    if (operation == Stop) {
      intcode
    } else {
      val nextState = step(operation, intcode)
      process(nextState)
    }
  }

  // Load and parse input data to an Intcode program
  val inputProgram: IO[Intcode] = for {
    f <- File.readLines("/day2.txt")
    allLinesConcatenated = f.mkString(",")
    memory <- allLinesConcatenated
      .split(",")
      .toVector
      .traverse(str => IO(str.toInt))
  } yield Intcode(0, memory)

  val solvePuzzle1: IO[Vector[Int]] = for {
    initial <- inputProgram
    initialStateBeforeComputerCaughtFire = initial.copy(
      memory = initial.memory.updated(1, 12).updated(2, 2)
    )
    result = process(initialStateBeforeComputerCaughtFire)
    _ <- Console.putStrLn(s"Solution for puzzle 1: ${result.memory(0)}")
  } yield result.memory

  val puzzle2Combinations: IndexedSeq[(Int, Int)] = for {
    noun <- 0 to 99
    verb <- 0 to 99
  } yield (noun, verb)

  def attemptNounVerbCombination(noun: Int,
                                 verb: Int): IO[(Boolean, Int, Int)] = {
    for {
      initial <- inputProgram
      updatedInitialState = initial.copy(
        memory = initial.memory.updated(1, noun).updated(2, verb)
      )
      result = process(updatedInitialState)
    } yield (result.memory(0) == 19690720, noun, verb)
  }

  val solvePuzzle2: IO[(Int, Int)] = for {
    puzzle2 <- puzzle2Combinations.toVector.parTraverse {
      case (n, v) => attemptNounVerbCombination(n, v)
    }
    answer <- IO(
      puzzle2.find(_._1).map(solution => (solution._2, solution._3)).get
    )
    _ <- Console.putStrLn(s"Solution for puzzle 2: $answer")
  } yield answer

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- solvePuzzle1
      _ <- solvePuzzle2
    } yield ExitCode.Success
}
