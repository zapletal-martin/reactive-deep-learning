import Node.Input

class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case a: Double => outputs.foreach(_ ! Input(a))
  }
}