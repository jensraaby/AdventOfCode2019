package util

import cats.effect.IO

import scala.io.StdIn

object Console {

  def putStrLn(string: String): IO[Unit] = IO(println(string))

  def readLn: IO[String] = IO(StdIn.readLine)

}
