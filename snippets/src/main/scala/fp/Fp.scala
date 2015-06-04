package fp

import scala.math._

object Fp {

  scala.io.Source.fromFile("src/main/resources/data2.csv")
    .getLines()
    .toList
    .par
    .foreach{ l =>
      val splits = l.split(",")
      val output = Network.feedForward(Vector(splits(0).toDouble, splits(1).toDouble, splits(2).toDouble), network)
    }

  val network = Seq[Vector[Vector[Double] => Double]](
    Vector(
      Perceptron.activation(Vector(0.3, 0.3, 0.3), _, 0.2, Neuron.sigmoid),
      Perceptron.activation(Vector(0.3, 0.3, 0.3), _, 0.2, Neuron.sigmoid)),
    Vector(Perceptron.activation(Vector(0.3, 0.3, 0.3), _, 0.2, Neuron.sigmoid))
  )
}

object Network {
  def feedForward(features: Vector[Double], network: Seq[Vector[Vector[Double] => Double]]): Vector[Double] =
    network.foldLeft(features)((b, a) => a.map(_(b)))
}

object Perceptron {
  def activation(w: Vector[Double], f: Vector[Double], bias: Double, activationFunction: Double => Double) =
    activationFunction(w.zip(f).map(x => x._1 * x._2).sum + bias)
}

object Neuron {
  val simple: Double => Double = x => if(x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}
