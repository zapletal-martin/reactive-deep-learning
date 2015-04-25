package oo

import oo.Node.{WeightedInput, Input}

class Edge(val in: Node, val out: Node) {
  var weight: Double = 0.3

  def run(in: Input) = out.run(WeightedInput(in.feature, weight))
}
