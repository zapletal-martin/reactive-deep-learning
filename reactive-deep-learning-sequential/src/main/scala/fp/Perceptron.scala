package fp

object Perceptron {
  def activation(w: Seq[Double], f: Seq[Double], bias: Double, activationFunction: Double => Double) =
    activationFunction(w.zip(f).map(x => x._1 * x._2).sum + bias)
}

/*
  def layerFeedForward(depth: Int): Seq[Double] =
    network(depth).map(_.apply(if(depth == 0) features else layerFeedForward(depth - 1), weights(depth), bias(depth)))

  val firstLayerOutput = network(0).map(_.apply(features, weights, bias(0)))
  network(1).map(_.apply(firstLayerOutput, weights, bias(1)))

  layerFeedForward(1)
*/

object Network {
  def feedForward(
      network: Seq[Seq[(Seq[Double], Seq[Double], Double) => Double]],
      features: Seq[Double],
      weights: Seq[Seq[Double]],
      bias: Seq[Double]): Seq[Double] = {

    network.foldLeft((0, features))((b, a) => (b._1 + 1, a.map(_(weights(b._1), b._2, bias(b._1)))))._2
  }
}