import Node._
import akka.typed.{Behavior, Props, ActorRef}
import akka.typed.ScalaDSL._

object Perceptron {
  import HasInputs._
  import HasOutputs._
  import Neuron._

  def props() = Props(receive)

  def receive = addInput(addOutput(run(_, _, 0.2, sigmoid, Seq(), Seq()), _)) // run && addInput && addOutput

  private def allInputsAvailable(w: Seq[Double], f: Seq[Double], in: Seq[ActorRef[Nothing]]) =
    w.length == in.length && f.length == in.length

  def run(
      inputs: Seq[ActorRef[Nothing]],
      outputs: Seq[ActorRef[Input]],
      bias: Double,
      activationFunction: Double => Double,
      weightsT: Seq[Double],
      featuresT: Seq[Double]): Behavior[NodeMessage] = Partial[NodeMessage] {

    case WeightedInput(f, w) =>
      val featuresTplusOne = featuresT :+ f
      val weightsTplusOne = weightsT :+ w

      if(allInputsAvailable(featuresTplusOne, weightsTplusOne, inputs)) {
        val activation = activationFunction(weightsTplusOne.zip(featuresTplusOne).map(x => x._1 * x._2).sum + bias)

        outputs.foreach(_ ! Input(activation))
        run(inputs, outputs, bias, activationFunction, Seq(), Seq())
      } else {
        run(inputs, outputs, bias, activationFunction, weightsTplusOne, featuresTplusOne)
      }
  }
}