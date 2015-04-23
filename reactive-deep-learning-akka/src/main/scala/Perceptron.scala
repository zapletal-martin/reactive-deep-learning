import Node._

class Perceptron() extends Neuron {

  override var weight: Double = 0.2
  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.1

  var weightsT: Seq[Double] = Seq()
  var featuresT: Seq[Double] = Seq()

  override def receive = run orElse addInput orElse addOutput

  def run: Receive = {
    case Input(f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(weightsT.length == inputNodes.length && featuresT.length == inputNodes.length) {
        val output = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Seq()
        weightsT = Seq()

        outputNodes.foreach(_ ! Input(output, weight))
      }
  }
}