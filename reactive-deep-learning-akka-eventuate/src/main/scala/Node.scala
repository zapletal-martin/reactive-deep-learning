import Node.{AddOutputs, Ack, AddInputs}
import akka.actor.{ActorRef, Actor}

object Node {
  case class Input(feature: Double)
  case class WeightedInput(feature: Double, weight: Double)

  case class AddInputs(input: Seq[ActorRef])
  case class AddOutputs(output: Seq[ActorRef])
  case object Ack
}

trait Node extends Actor

trait HasInputs extends Node {
  var inputs: Seq[ActorRef] = Seq()

  def addInput(): Receive = {
    case AddInputs(i) =>
      inputs = i
      sender() ! Ack
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[ActorRef] = Seq()

  def addOutput(): Receive = {
    case AddOutputs(o) =>
      outputs = o
      sender() ! Ack
  }
}