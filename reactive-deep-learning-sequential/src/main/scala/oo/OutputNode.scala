package oo

import oo.Node.WeightedInput

class OutputNode extends HasInputs {
  override def run(in: WeightedInput): Unit = println(s"Output: ${in.feature}")
}
