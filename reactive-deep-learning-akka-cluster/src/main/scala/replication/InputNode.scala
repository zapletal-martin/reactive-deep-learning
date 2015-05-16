package replication

import akka.actor.Props
import replication.Node.Input

object InputNode {
  def props(): Props = Props[InputNode]
}

class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case Input(f) => outputs.foreach(_ ! Input(f))
  }
}