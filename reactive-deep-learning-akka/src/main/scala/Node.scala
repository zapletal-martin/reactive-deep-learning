import Node.{Input, Ack, AddOutputs, AddInputs}
import akka.typed.ActorRef
import akka.typed.ScalaDSL.Static

object Node {
  case class Input(feature: Double)
  case class WeightedInput(feature: Double, weight: Double)

  case object Ack
  case class AddInputs(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack])
  case class AddOutputs(outputs: Seq[ActorRef[Input]], replyTo: ActorRef[Ack])
}

trait Node //extends Actor

trait HasInputs extends Node {
  var inputs: Seq[ActorRef[Nothing]] = Seq()

  val addInput = Static[AddInputs] { msg =>
      inputs = msg.inputs
      msg.replyTo ! Ack
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[ActorRef[Input]] = Seq()

  val addOutput = Static[AddOutputs] { msg =>
      outputs = msg.outputs
      msg.replyTo ! Ack
  }
}