import Node.InputCommand
import akka.actor.{ActorRef, Props}

object InputNode {
  def props(): Props = Props[InputNode]
}

class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case i: InputCommand => outputs.foreach(_ ! i)
  }
}