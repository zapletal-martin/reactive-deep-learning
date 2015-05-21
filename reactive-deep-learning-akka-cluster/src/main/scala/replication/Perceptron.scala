package replication

import akka.actor.{ActorRef, Props}
import replication.Node._

object Perceptron {
  def props(): Props = Props[Perceptron]
}

class Perceptron() extends Neuron {

  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.2

  var weightsT: Vector[Double] = Vector()
  var featuresT: Vector[Double] = Vector()

  override def receive = run orElse addInput orElse addOutput

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[ActorRef]) =
    w.length == in.length && f.length == in.length

  def run: Receive = {
    case WeightedInput(f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(allInputsAvailable(weightsT, featuresT, inputs)) {
        val activation = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Vector()
        weightsT = Vector()

        outputs.foreach(_ ! Input(activation))
      }

    case UpdateBias(b) =>
      bias = b
  }
}