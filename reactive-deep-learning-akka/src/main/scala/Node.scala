import Node.{AddOutput, AddInput}
import akka.actor._

object Node {
  case class Input(feature: Double)
  case class WeightedInput(feature: Double, weight: Double)

  /*case object GetWeight
  case class Weight(weight: Double)*/

  case class AddInput(input: Seq[ActorRef])
  case class AddOutput(output: Seq[ActorRef])
}

trait Node extends Actor

trait HasInputs extends Node {
  var inputs: Seq[ActorRef] = Seq()
  def addInput: Receive = { case AddInput(i) => inputs = i }
}

trait HasOutputs extends Node {
  var outputs: Seq[ActorRef] = Seq()
  def addOutput: Receive = { case AddOutput(o) => outputs = o }
}