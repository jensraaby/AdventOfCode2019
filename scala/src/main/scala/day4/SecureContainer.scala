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

  def isGood(num: Int): Boolean = {
    val digits = numToDigits(num)
    digits.size == 6 && isIncreasing(digits) && containsTwoSameAdjacent(digits)
  }

  def findValidPasswords(start: Int, end: Int): Vector[Int] = {
    for {
      x <- (start to end).toVector
      if isGood(x)
    } yield x
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- Console.putStrLn("Guessing passwords. Printing first and last 5")
      passwords = findValidPasswords(134792, 675810)
      _ <- passwords
        .take(5)
        .traverse(password => Console.putStrLn(s"Guess: $password"))
      _ <- Console.putStrLn("...")
      _ <- passwords
        .takeRight(5)
        .traverse(password => Console.putStrLn(s"Guess: $password"))
      _ <- Console.putStrLn(s"\nTotal number of guesses: ${passwords.size}")
    } yield ExitCode.Success
}
