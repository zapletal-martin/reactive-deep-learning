import Node.{NodeMessage, Input}
import akka.typed.{Behavior, ActorRef, Props}
import akka.typed.ScalaDSL._
import HasOutputs._

object InputNode {

  def props() = Props(receive)

  def receive = addOutput(run, Seq())

  def run(inputs: Seq[ActorRef[Nothing]], outputs: Seq[ActorRef[Input]]): Behavior[NodeMessage] = Partial[NodeMessage] {
    case i: Input =>
      outputs.foreach(_ ! i)
      run(inputs, outputs)
  }
}