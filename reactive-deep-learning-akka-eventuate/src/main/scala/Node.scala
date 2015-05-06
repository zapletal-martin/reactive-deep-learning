import Node.{AddOutputsCommand, Ack, AddInputsCommand}
import akka.actor.{Actor, ActorRef}

object Node {
  case class InputCommand(feature: Double)
  case class WeightedInputCommand(feature: Double, weight: Double)

  case class AddInputsCommand(input: Seq[ActorRef])
  case class AddOutputsCommand(output: Seq[ActorRef])
  case object Ack

  case class UpdateBiasCommand(bias: Double)
  case class UpdatedBiasEvent(bias: Double)

  case class UpdateWeightCommand(weight: Double)
  case class UpdatedWeightEvent(weight: Double)
}

trait Node extends Actor

trait HasInputs extends Node {
  var inputs: Seq[ActorRef] = Seq()

  def addInput(): Receive = {
    case AddInputsCommand(i) =>
      inputs = i
      sender() ! Ack
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[ActorRef] = Seq()

  def addOutput(): Receive = {
    case AddOutputsCommand(o) =>
      outputs = o
      sender() ! Ack
  }
}