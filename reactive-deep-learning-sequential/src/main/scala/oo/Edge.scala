package oo

import oo.Node.{WeightedInput, Input}

trait HasInput {
  var input: Node = _
  def addInput(i: Node): Unit = input = i
}

trait HasOutput {
  var output: Node = _
  def addOutput(o: Node): Unit = output = o
}

class Edge extends HasInput with HasOutput {
  var weight: Double = 0.3

  def run(in: Input) = output.run(WeightedInput(in.feature, weight))
}
