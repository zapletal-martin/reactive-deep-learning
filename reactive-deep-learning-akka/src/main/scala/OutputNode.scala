import Node.WeightedInput

class OutputNode() extends HasInputs {
  override def receive = run orElse addInput

  def run: Receive = {
    case WeightedInput(a, w) => println(s"Output: $a")
  }
}