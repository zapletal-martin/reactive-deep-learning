import Node.{NodeId, AddInput, AddOutput}
import akka.actor._

object Node {
  type NodeId = String

  case class Input(recipient: NodeId, feature: Double)
  case class WeightedInput(recipient: NodeId, feature: Double, weight: Double)

  case class AddInput(recipient: NodeId, input: Seq[NodeId])
  case class AddOutput(recipient: NodeId, output: Seq[NodeId])
}

trait Node extends Actor

trait HasInputs extends Node {
  var inputs: Seq[NodeId] = Seq()
  def addInput(): Receive = { case AddInput(r, i) => inputs = i }
}

trait HasOutputs extends Node {
  var outputs: Seq[NodeId] = Seq()
  def addOutput(): Receive = { case AddOutput(r, o) => outputs = o }
}