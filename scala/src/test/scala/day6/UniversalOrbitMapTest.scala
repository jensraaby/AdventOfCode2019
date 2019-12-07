package day6

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class UniversalOrbitMapTest extends AnyFunSuite with Matchers {

  test("Parse map") {
    val lines = List("COM)B", "B)C")

    UniversalOrbitMap.parse(lines).unsafeRunSync() shouldBe (
      mutable.Map("COM" -> List("B"), "B" -> List("C")),
      mutable.Map("COM" -> List("B"), "B" -> List("COM", "C"), "C" -> List("B"))
    )
  }

  test("count orbiters") {
    val tree1 = mutable.Map("COM" -> List("B"), "B" -> List("C"))

    UniversalOrbitMap.countOrbiters(tree1, "COM") shouldBe 3
    UniversalOrbitMap.countOrbiters(tree1, "B") shouldBe 1

    val tree2 = mutable.Map(
      "COM" -> List("B"),
      "B" -> List("C"),
      "C" -> List("D"),
      "D" -> List("E")
    )

    UniversalOrbitMap.countOrbiters(tree2, "COM") shouldBe 10
  }

  test("example map") {
    val lines = List(
      "COM)B",
      "B)C",
      "C)D",
      "D)E",
      "E)F",
      "B)G",
      "G)H",
      "D)I",
      "E)J",
      "J)K",
      "K)L",
    )

    val (tree, graph) = UniversalOrbitMap.parse(lines).unsafeRunSync()

    UniversalOrbitMap.countOrbiters(tree, "COM") shouldBe 42
  }

  test("Orbital transfers between") {
    val lines = List(
      "COM)B",
      "B)C",
      "C)D",
      "D)E",
      "E)F",
      "B)G",
      "G)H",
      "D)I",
      "E)J",
      "J)K",
      "K)L",
      "K)YOU",
      "I)SAN"
    )

    val (_, graph) = UniversalOrbitMap.parse(lines).unsafeRunSync()
    UniversalOrbitMap.orbitalTransfersBetween("YOU", "SAN", graph) shouldBe 4
  }
}
