import Node.Input
import akka.actor.{Actor, ActorRef}

class InputNode() extends HasOutputs {
  override var outputNodes: Seq[ActorRef] = Seq()

  override def receive = run orElse addOutput

  def run: Actor.Receive = {
    case a: Double => outputNodes.foreach(_ ! Input(a, 1))
  }
}