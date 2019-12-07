package day6

import cats.effect.{ExitCode, IO, IOApp}
import util.{Console, File}

import scala.collection.mutable

object UniversalOrbitMap extends IOApp {
  type Tree = mutable.Map[String, List[String]]

  def parse(lines: List[String]): IO[
    (mutable.Map[String, List[String]], mutable.Map[String, List[String]])
  ] = IO {
    // store Object -> its orbiters
    val objectTree = mutable.Map[String, List[String]]()
    // store object -> orbiters and orbiter -> objects
    val objectGraph = mutable.Map[String, List[String]]()

    val beingOrbited: List[List[String]] =
      lines.map(line => line.split("\\)").toList)

    beingOrbited.foreach {
      case orbited :: orbiter :: Nil =>
        objectTree.updateWith(orbited) {
          case Some(items) => Some(items.appended(orbiter))
          case None        => Some(List(orbiter))
        }
        objectGraph.updateWith(orbited) {
          case Some(items) => Some(items.appended(orbiter))
          case None        => Some(List(orbiter))
        }
        objectGraph.updateWith(orbiter) {
          case Some(items) => Some(items.appended(orbited))
          case None        => Some(List(orbited))
        }
      case _ => throw new RuntimeException("Invalid line")
    }
    (objectTree, objectGraph)
  }

  def countOrbiters(tree: Tree, leaf: String, depth: Int = 0): Int = {
    val children = tree.getOrElse(leaf, List())
    depth + children.map(child => countOrbiters(tree, child, depth + 1)).sum
  }

  def orbitalTransfersBetween(start: String, end: String, graph: Tree): Int = {
    var queue = Vector(Vector(start))
    while (queue.nonEmpty) {
      // get the first path in the queue and remove it
      val path = queue.take(1).head
      queue = queue.drop(1)

      // look up all adjacent nodes (children) from the last (i.e. most recent) node in the path
      graph(path.last).foreach { child =>
        if (path.contains(child)) {
          // do nothing - we've already been here
        } else if (child == end) {
          // done! Take the size of the path - 2 for the initial and final hops
          return path.size - 2
        } else {
          // add the path with this child to the end of the queue
          queue = queue :+ (path :+ child)
        }
      }
    }
    0
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      inputLines <- File.readLines("/day6.txt")
      parsed <- parse(inputLines.toList)
      count = countOrbiters(parsed._1, "COM")
      _ <- Console.putStrLn(s"Number of orbits is $count")
      orbitalTransfersRequired = orbitalTransfersBetween(
        "YOU",
        "SAN",
        parsed._2
      )
      _ <- Console.putStrLn(
        s"Number of transfers from YOU to SAN is $orbitalTransfersRequired"
      )
    } yield ExitCode.Success
}
