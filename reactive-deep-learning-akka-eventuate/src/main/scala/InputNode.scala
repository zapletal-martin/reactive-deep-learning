import Node.Input
import akka.actor.Props

object InputNode {
  def props(): Props = Props[InputNode]
  val shardName: String = "InputNode"
}

class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case i: Input => outputs.foreach(_ ! i)
  }
}