package oo

import oo.Node.{Input, WeightedInput}

class InputNode extends HasOutputs {
  override def run(in: WeightedInput): Unit = outputs.foreach(_.run(Input(in.feature)))
}
