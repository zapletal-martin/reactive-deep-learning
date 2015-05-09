package fp

import java.text.SimpleDateFormat
import java.util.Date

object Main extends App {
  override def main (args: Array[String]) {

    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    val network = Seq[Seq[(Seq[Double], Seq[Double], Double) => Double]](
      Seq(Perceptron.activation(_, _, _, Neuron.sigmoid), Perceptron.activation(_, _, _, Neuron.sigmoid)),
      Seq(Perceptron.activation(_, _, _, Neuron.sigmoid))
    )

    scala.io.Source.fromFile("src/main/resources/data.csv")
      .getLines()
      .toList
      .par
      .foreach{ l =>
      val splits = l.split(",")

      val output = Network.feedForward(
        network,
        Seq(splits(0).toDouble, splits(1).toDouble, splits(2).toDouble),
        Seq(Seq(0.3, 0.3, 0.3), Seq(0.3, 0.3)),
        Seq(0.2, 0.2))

      println(s"Output with result $output in ${format.format(new Date(System.currentTimeMillis()))}")
    }
  }
}
