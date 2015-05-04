import Node._
import Edge.EdgeMessage
import akka.typed.ActorRef
import akka.typed.ScalaDSL.Static

object Node {
  trait NodeMessage
  case class Input(feature: Double) extends NodeMessage with EdgeMessage
  case class WeightedInput(feature: Double, weight: Double) extends NodeMessage

  case object Ack
  case class AddInputs(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NodeMessage
  case class AddOutputs(outputs: Seq[ActorRef[Input]], replyTo: ActorRef[Ack.type]) extends NodeMessage
}

trait Node //extends Actor

trait HasInputs extends Node {
  var inputs: Seq[ActorRef[Nothing]] = Seq()

  val addInput = Static[NodeMessage] {
    case AddInputs(i, r) =>
      inputs = i
      r ! Ack
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[ActorRef[Input]] = Seq()

  val addOutput = Static[NodeMessage] {
    case AddOutputs(o, r) =>
      outputs = o
      r ! Ack
  }
}