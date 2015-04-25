package array

object Main extends App {
  val > = Array
  val o = Some
  val x = None

  override def main (args: Array[String]) {
    val layers = Array(3, 2, 1)
    val a = scala.None

    val topology: Array[Array[Option[Double]]] =
      >(
        >(x, x, x, o(.2), o(.2), x),
        >(x, x, x, o(.2), o(.2), x),
        >(x, x, x, o(.2), o(.2), x),
        >(x, x, x, x, x, o(.2)),
        >(x, x, x, x, x, o(.2)),
        >(x, x, x, x, x, x)
      )

    scala.io.Source.fromFile("src/main/resources/data.csv")
      .getLines()
      .foreach { l =>
      val splits = l.split(",")

      Network.process(topology, layers, splits.map(_.toDouble))
    }

  }
}
