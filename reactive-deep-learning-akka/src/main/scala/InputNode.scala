import Node.Input
import akka.actor.{Actor, ActorRef}

class InputNode() extends HasOutputs {
  override var outputs: Seq[ActorRef] = Seq()

  override def receive = run orElse addOutput

  def run: Actor.Receive = {
    case a: Double => outputs.foreach(_ ! Input(a))
  }
}