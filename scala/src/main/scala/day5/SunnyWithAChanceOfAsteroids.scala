package day5

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import util.{Console, File}

object SunnyWithAChanceOfAsteroids extends IOApp {

  type Address = Int
  sealed trait Op
  case object Add extends Op
  case object Multiply extends Op
  case object In extends Op
  case object Out extends Op
  case object Stop extends Op

  sealed trait ParamMode
  case object Position extends ParamMode
  case object Immediate extends ParamMode

  case class Computer(program: Vector[Int],
                      pointer: Address,
                      output: Vector[Int]) {

    def execute(input: Int): Option[Computer] = {
      val (op, paramModes, inOffsets, outOffsets) = decodeInstruction(
        program(pointer)
      )
      val inputs = inOffsets.map(offset => program(pointer + offset))
      val outputs = outOffsets.map(offset => program(pointer + offset))

      val paramModesToArgAddresses = paramModes.zip(inputs)
      val parameters = paramModesToArgAddresses.map(loadParameters(program))

      val (newOutputs, newProgram) = step(op, parameters, outputs, input)
      val newPointer = pointer + inOffsets.size + outOffsets.size + 1

      if (op == Stop) {
        None
      } else {
        Some(Computer(newProgram, newPointer, newOutputs))
      }
    }

    private def step(op: Op,
                     arguments: Vector[Int],
                     outputAddresses: Vector[Int],
                     programInput: Int) = {
      op match {
        case Add =>
          (output, program.updated(outputAddresses.head, arguments.sum))
        case Multiply =>
          (output, program.updated(outputAddresses.head, arguments.product))
        case In =>
          (output, program.updated(outputAddresses.head, programInput))
        case Out =>
          (output.prepended(arguments.head), program)
        case Stop => (output, program)
      }
    }
  }

  object Computer {
    def apply(program: Vector[Int]): Computer = Computer(program, 0, Vector())
  }

  def loadParameters(
    program: Vector[Int]
  )(modeAndAddress: (ParamMode, Int)): Address = {
    modeAndAddress match {
      case (Position, address)  => program(address)
      case (Immediate, address) => address
    }
  }

  def decodeInstruction(
    instruction: Int
  ): (Op, Vector[ParamMode], Vector[Address], Vector[Address]) = {
    val op = opCode(instruction)
    val paramModes = modes(instruction)

    val insAndOuts = op match {
      case Add      => (Vector(1, 2), Vector(3))
      case Multiply => (Vector(1, 2), Vector(3))
      case In       => (Vector(), Vector(1))
      case Out      => (Vector(1), Vector())
      case Stop     => (Vector(), Vector())
    }

    (op, paramModes, insAndOuts._1, insAndOuts._2)
  }

  def opCode(code: Int): Op = code % 100 match {
    case 1  => Add
    case 2  => Multiply
    case 3  => In
    case 4  => Out
    case 99 => Stop
    case other =>
      throw new RuntimeException(s"Unexpected operation code $other")
  }

  def modes(code: Int): Vector[ParamMode] = {
    val digits = Vector(code / 100, code / 1000, code / 10000)
    digits.map(_ % 2).map {
      case 0 => Position
      case 1 => Immediate
    }
  }

  def evaluateProgram(program: Vector[Int], input: Int): Vector[Int] = {
    val initialComputer = Computer(program)

    @scala.annotation.tailrec
    def process(computer: Computer): Vector[Int] =
      computer.execute(input) match {
        case Some(newComputer) => process(newComputer)
        case None              => computer.output
      }

    process(initialComputer)
  }

  // Load and parse input data to an Intcode program
  val loadProgram: IO[Vector[Int]] = for {
    lines <- File.readLines("/day5.txt")
    memoryStrings <- IO(lines.head.split(",").toVector)
    codes <- memoryStrings.traverse(str => IO(str.toInt))
  } yield codes

  override def run(args: List[String]): IO[ExitCode] =
    for {
      testProgram <- loadProgram
      _ <- Console.putStrLn("Running program...")
      outputs = evaluateProgram(testProgram, input = 1)
      _ <- Console.putStrLn("Completed execution. Outputs:")
      _ <- Console.putStrLn(outputs.mkString(","))
    } yield ExitCode.Success
}
