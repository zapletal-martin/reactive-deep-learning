import Node.{WeightedInput, Input, Ack}
import Edge.AddInput
import akka.typed.{ActorRef, Behavior, Props}
import akka.typed.ScalaDSL._

object HasInput {
  def addInput[T](behavior: ActorRef[Nothing] => Behavior[T]) = Partial[T] {
    case AddInput(i, r) =>
      r ! Ack
      behavior(i)
  }
}

object HasOutput {
  import Edge._

  def addOutput[T](behavior: (ActorRef[Nothing], ActorRef[WeightedInput]) => Behavior[T], input: ActorRef[Nothing]) = Partial[T] {
    case AddOutput(o, r) =>
      r ! Ack
      behavior(input, o)
  }
}

object Edge {
  import HasInput._
  import HasOutput._

  trait EdgeMessage
  case class AddInput(input: ActorRef[Nothing], replyTo: ActorRef[Ack.type]) extends EdgeMessage
  case class AddOutput(output: ActorRef[WeightedInput], replyTo: ActorRef[Ack.type]) extends EdgeMessage
  case class UpdateWeight(weight: Double) extends EdgeMessage

  def props() = Props(receive)

  def receive = addInput(addOutput(run(_, _, 0.3), _))

  def run(input: ActorRef[Nothing], output: ActorRef[WeightedInput], weight: Double): Behavior[EdgeMessage] = Partial[EdgeMessage] {
    case Input(f) =>
      output ! WeightedInput(f, weight)
      run(input, output, weight)

    case UpdateWeight(newWeight) =>
      run(input, output, newWeight)
  }
}
