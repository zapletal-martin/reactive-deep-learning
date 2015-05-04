import Node.{WeightedInput, Input, Ack}
import Edge.{AddOutput, AddInput}
import akka.typed.{Props, ActorRef}
import akka.typed.ScalaDSL.{Or, Static}

object Edge {
  case class AddInput(input: ActorRef[Nothing], replyTo: ActorRef[Ack])
  case class AddOutput(output: ActorRef[WeightedInput], replyTo: ActorRef[Ack])

  def props(): Props = Props[Edge]
}

trait HasInput {
  var input: ActorRef[Nothing] = _

  val addInput = Static[AddInput] { msg =>
      input = msg.input
      msg.replyTo ! Ack
  }
}

trait HasOutput {
  var output: ActorRef[WeightedInput] = _

  val addOutput = Static[AddOutput] { msg =>
    output = msg.output
    msg.replyTo ! Ack
  }
}

class Edge extends HasInput with HasOutput {
  var weight: Double = 0.3

  def receive = Or(Or(run, addOutput), addInput)

  def run = Static {
    case Input(f) => output ! WeightedInput(f, weight)
  }
}
