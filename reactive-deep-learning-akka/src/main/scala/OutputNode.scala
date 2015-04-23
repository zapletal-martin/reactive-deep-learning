import Node.WeightedInput
import akka.actor.ActorRef

class OutputNode() extends HasInputs {
  override var inputs: Seq[ActorRef] = Seq()

  override def receive = run orElse addInput

  def run: Receive = {
    case WeightedInput(a, w) => println(s"Output: $a")
  }
}