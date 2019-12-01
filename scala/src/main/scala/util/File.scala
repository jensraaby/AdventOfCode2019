package util

import cats.effect.{IO, Resource}

import scala.io.{BufferedSource, Source}

object File {

  def readLines(path: String): IO[Vector[String]] =
    for {
      lines <- classpathResource(path).use { buf => IO(buf.getLines().toVector) }
    } yield lines

  def classpathResource(path: String): Resource[IO, BufferedSource] = {
    for {
      stream <- Resource.fromAutoCloseable(IO(getClass.getResourceAsStream(path)))
      source <- Resource.fromAutoCloseable(IO(Source.fromInputStream(stream)))
    } yield source
  }

}
