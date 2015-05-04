import Node.{WeightedInput, Input, Ack}
import Edge.{EdgeMessage, AddOutput, AddInput}
import akka.typed.{Behavior, Props, ActorRef}
import akka.typed.ScalaDSL.{Or, Static}

trait HasInput {
  var input: ActorRef[Nothing] = _

  val addInput: Behavior[EdgeMessage] = Static[EdgeMessage] {
    case AddInput(i, r) =>
      input = i
      r ! Ack
  }
}

trait HasOutput {
  var output: ActorRef[WeightedInput] = _

  val addOutput: Behavior[EdgeMessage] = Static[EdgeMessage] {
    case AddOutput(o, r) =>
      output = o
      r ! Ack
  }
}

object Edge extends HasInput with HasOutput {
  trait EdgeMessage
  case class AddInput(input: ActorRef[Nothing], replyTo: ActorRef[Ack.type]) extends EdgeMessage
  case class AddOutput(output: ActorRef[WeightedInput], replyTo: ActorRef[Ack.type]) extends EdgeMessage

  def props() = Props(receive)

  var weight: Double = 0.3

  def receive = Or(Or(run, addOutput), addInput)

  def run: Behavior[EdgeMessage] = Static[EdgeMessage] {
    case Input(f) =>
    output ! WeightedInput(f, weight)
  }
}