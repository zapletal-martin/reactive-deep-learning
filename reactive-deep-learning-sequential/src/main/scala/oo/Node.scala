package oo

import oo.Node.WeightedInput

object Node {
  case class Input(feature: Double)
  case class WeightedInput(feature: Double, weight: Double)
}

trait Node {
  def run(in: WeightedInput)
}

trait HasInputs extends Node {
  var inputs: Seq[Edge] = Seq()
  def addInput(i: Seq[Edge]): Unit = inputs = i
}

trait HasOutputs extends Node {
  var outputs: Seq[Edge] = Seq()
  def addOutput(i: Seq[Edge]): Unit = outputs = i
}