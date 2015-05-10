import Node._
import Edge.EdgeMessage
import akka.typed.{Behavior, ActorRef}
import akka.typed.ScalaDSL._

object Node {
  trait NodeMessage
  case class Input(feature: Double) extends NodeMessage with EdgeMessage
  case class WeightedInput(feature: Double, weight: Double) extends NodeMessage
  case class UpdateBias(bias: Double) extends NodeMessage

  case object Ack
  case class AddInputs(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NodeMessage
  case class AddOutputs(outputs: Seq[ActorRef[Input]], replyTo: ActorRef[Ack.type]) extends NodeMessage
}

object HasInputs {
  def addInput[T](behavior: Seq[ActorRef[Nothing]] =>  Behavior[T]) = Partial[T] {
    case AddInputs(i, r) =>
      r ! Ack
      behavior(i)
  }
}

object HasOutputs {
  def addOutput[T](behavior: (Seq[ActorRef[Nothing]], Seq[ActorRef[Input]]) =>  Behavior[T], inputs: Seq[ActorRef[Nothing]]) = Partial[T] {
    case AddOutputs(o, r) =>
      r ! Ack
      behavior(inputs, o)
  }
}