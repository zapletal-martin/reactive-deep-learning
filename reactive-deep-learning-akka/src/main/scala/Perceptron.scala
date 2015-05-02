import Node._
import akka.actor.{Props, ActorRef}

object Perceptron {
  def props(): Props = Props[Perceptron]
}

class Perceptron() extends Neuron {

  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.1

  var weightsT: Seq[Double] = Seq()
  var featuresT: Seq[Double] = Seq()

  override def receive = run orElse addInput orElse addOutput

  private def allInputsAvailable(w: Seq[Double], f: Seq[Double], in: Seq[ActorRef]) =
    w.length == in.length && f.length == in.length

  def run: Receive = {
    case WeightedInput(f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(allInputsAvailable(weightsT, featuresT, inputs)) {
        val output = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Seq()
        weightsT = Seq()

        outputs.foreach(_ ! Input(output))
      }
  }
}