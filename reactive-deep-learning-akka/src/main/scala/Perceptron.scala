import Node._
import akka.typed.{Behavior, Props, ActorRef}
import akka.typed.ScalaDSL._

object Perceptron {
  import HasInputs._
  import HasOutputs._
  import Neuron._

  def props() = Props(behaviour)

  def behaviour = addInput(addOutput(feedForward(_, _, 0.2, sigmoid, Vector(), Vector()), _))

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[ActorRef[Nothing]]) =
    w.length == in.length && f.length == in.length

  def feedForward(
      inputs: Seq[ActorRef[Nothing]],
      outputs: Seq[ActorRef[Input]],
      bias: Double,
      activationFunction: Double => Double,
      weightsT: Vector[Double],
      featuresT: Vector[Double]): Behavior[NodeMessage] = Partial[NodeMessage] {

    case WeightedInput(f, w) =>
      val featuresTplusOne = featuresT :+ f
      val weightsTplusOne = weightsT :+ w

      if(allInputsAvailable(featuresTplusOne, weightsTplusOne, inputs)) {
        //println(s"Weights $weightsTplusOne, features $featuresTplusOne")
        val activation = activationFunction(weightsTplusOne.zip(featuresTplusOne).map(x => x._1 * x._2).sum + bias)
        outputs.foreach(_ ! Input(activation))

        feedForward(inputs, outputs, bias, activationFunction, Vector(), Vector())
      } else {
        feedForward(inputs, outputs, bias, activationFunction, weightsTplusOne, featuresTplusOne)
      }

    case UpdateBias(newBias) =>
      feedForward(inputs, outputs, newBias, activationFunction, weightsT, featuresT)
  }
}