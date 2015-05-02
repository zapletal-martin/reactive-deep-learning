import Node.Input
import akka.actor.Props

object InputNode {
  def props(): Props = Props[InputNode]
}

class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case a: Input => outputs.foreach(_ ! a)
  }
}