package oo

import Node.Input
import oo.Node.WeightedInput

class Perceptron extends Neuron {
  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.2

  var weightsT: Seq[Double] = Seq()
  var featuresT: Seq[Double] = Seq()

  private def allInputsAvailable(w: Seq[Double], f: Seq[Double], in: Seq[Edge]) =
    w.length == in.length && f.length == in.length

  override def run(in: WeightedInput): Unit = {
    featuresT = featuresT :+ in.feature
    weightsT = weightsT :+ in.weight

    //println(s"Features size ${featuresT.size} weights size ${weightsT.size}")

    if(allInputsAvailable(weightsT, featuresT, inputs)) {
      val activation = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

      featuresT = Seq()
      weightsT = Seq()

      outputs.foreach(_.run(Input(activation)))
    }
  }
}
