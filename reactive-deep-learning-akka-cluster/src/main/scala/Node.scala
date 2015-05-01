import Node.{NodeId, AddInput, AddOutput}
import akka.actor._
import akka.contrib.pattern.ShardRegion

object Node {
  type NodeId = String

  case class Input(recipient: NodeId, feature: Double)
  case class WeightedInput(recipient: NodeId, feature: Double, weight: Double)

  case class AddInput(recipient: NodeId, input: Seq[NodeId])
  case class AddOutput(recipient: NodeId, output: Seq[NodeId])

  val idExtractor: ShardRegion.IdExtractor = {
    case a: AddInput => (a.recipient.toString, a)
    case o: AddOutput => (o.recipient.toString, o)
    case s: WeightedInput => (s.feature.toString, s)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case a: AddInput => (a.recipient.hashCode % 100).toString
    case o: AddOutput => (o.recipient.hashCode % 100).toString
    case s: WeightedInput => (s.feature.hashCode % 100).toString
  }
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