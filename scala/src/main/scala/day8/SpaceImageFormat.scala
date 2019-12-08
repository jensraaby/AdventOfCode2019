package day8

import cats.effect.{ExitCode, IO, IOApp}
import util.{Console, File}
object SpaceImageFormat extends IOApp {

  type Image = Vector[Vector[Vector[Int]]]

  def parseImage(input: String, width: Int, height: Int): IO[Image] = IO {
    input.toCharArray
      .map(_.asDigit) // parse characters as digits (ints)
      .toVector
      .grouped(width) // batches of width
      .toVector
      .grouped(height) // batches of height
      .toVector
  }

  def layerWithFewest0Digits(parsedImage: Image): Vector[Int] =
    parsedImage
      .map(_.flatten) // flatten the pixels in each layer to a single Vector
      .minBy(_.count(_ == 0))

  def countOfDigit(arr: Vector[Int], digit: Int): Int = arr.count(_ == digit)

  def pixelAt(image: Image, x: Int, y: Int): Int = {
    val pixelAtAllLayers = image.map(layer => layer(y)(x))
    pixelAtAllLayers.find(_ != 2).get // returns the first non-transparent pixel
  }

  def renderImageAsString(image: Image, width: Int, height: Int): String = {
    val pixels: IndexedSeq[Int] = for {
      y <- 0 until height
      x <- 0 until width
    } yield pixelAt(image, x, y)

    pixels
      .grouped(width)
      .map(row => row.map(int => if (int == 1) "#" else " ").mkString(""))
      .mkString("\n")
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      input <- File.readLines("/day8.txt")
      parsed <- parseImage(input.mkString, 25, 6)
      layerWithFewest = layerWithFewest0Digits(parsed)
      count = countOfDigit(layerWithFewest, 1) * countOfDigit(
        layerWithFewest,
        2
      )
      _ <- Console.putStrLn(
        s"Parsed layers: ${parsed.size}. Rows per layer: ${parsed.head.size}\nCount of 1s * count of 2s: $count"
      )
      _ <- Console.putStrLn(renderImageAsString(parsed, 25, 6))
    } yield ExitCode.Success
}
