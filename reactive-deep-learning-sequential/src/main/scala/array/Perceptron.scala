package array

object Perceptron {
  def output(features: Seq[Double], weights: Seq[Double], bias: Double): Double = {
    val activationFunction = ActivationFunction.sigmoid
    activationFunction(weights.zip(features).map(x => x._1 * x._2).sum + bias)
  }
}
