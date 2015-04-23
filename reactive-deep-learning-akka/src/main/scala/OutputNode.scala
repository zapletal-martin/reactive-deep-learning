import Node.Input
import akka.actor.ActorRef

class OutputNode() extends HasInputs {
  override var inputNodes: Seq[ActorRef] = Seq()

  override def receive = run orElse addInput

  def run: Receive = {
    case Input(a, w) => println(s"Output: $a")
  }
}