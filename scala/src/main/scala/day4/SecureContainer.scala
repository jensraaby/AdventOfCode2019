package day4

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import util.Console

object SecureContainer extends IOApp {

  def numToDigits(i: Int): List[Int] = {
    if (i > 0) {
      val mod10 = i % 10
      val div10 = i / 10
      numToDigits(div10) :+ mod10
    } else {
      List()
    }
  }

  @scala.annotation.tailrec
  def isIncreasing(nums: List[Int]): Boolean =
    nums match {
      case Nil            => true
      case _ :: Nil       => true
      case i :: j :: rest => (i <= j) && isIncreasing(j :: rest)
    }

  def containsTwoSameAdjacent(nums: List[Int]): Boolean =
    (nums.size > 1) && nums.sliding(2).exists(pair => pair(0) == pair(1))

  // This was quite tricky. Had a look at a Haskell implementation to work it out :/
  def hasValidDoubles(nums: List[Int]): Boolean = {
    val paddedNums = (10 :: nums) :+ 10 // we need padding so that the first / last pair can pass the filtering predicate
    val groupsOf4 = paddedNums.sliding(4) // get all the groups of 4 so we can identify pairs with no other matching neighbours
    val validAdjacents = groupsOf4 map {
      case a :: b :: c :: d :: Nil => a != b && b == c && c != d
      case _                       => throw new RuntimeException("Invalid groups of 4")
    }
    validAdjacents.exists(a => a) // this effectively does "OR" over all the groups of 4
  }

  def isGood(num: Int): Boolean = {
    val digits = numToDigits(num)
    digits.size == 6 && isIncreasing(digits) && containsTwoSameAdjacent(digits)
  }

  def findValidPasswords(start: Int, end: Int): Vector[Int] =
    for {
      x <- (start to end).toVector
      if isGood(x)
    } yield x

  def findValidPasswordsPart2(start: Int, end: Int): Vector[Int] =
    for {
      x <- (start to end).toVector
      if isGood(x) && hasValidDoubles(numToDigits(x))
    } yield x

  def solvePart1: IO[Unit] =
    for {
      _ <- Console.putStrLn(
        "Part 1: Guessing passwords. Printing first and last 5"
      )
      passwords = findValidPasswords(134792, 675810)
      _ <- passwords
        .take(5)
        .traverse(password => Console.putStrLn(s"Guess: $password"))
      _ <- Console.putStrLn("...")
      _ <- passwords
        .takeRight(5)
        .traverse(password => Console.putStrLn(s"Guess: $password"))
      _ <- Console.putStrLn(
        s"\nTotal number of valid guesses: ${passwords.size}"
      )
    } yield ()

  def solvePart2: IO[Unit] =
    for {
      _ <- Console.putStrLn("Part 2: Guessing passwords...")
      passwords = findValidPasswordsPart2(134792, 675810)
      _ <- Console.putStrLn(s"Total number of valid guesses: ${passwords.size}")
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- solvePart1
      _ <- solvePart2
    } yield ExitCode.Success
}
