import Node.{Ack, NodeId, AddInputs, AddOutputs}
import akka.actor._
import akka.contrib.pattern.ShardRegion

object Node {
  type NodeId = String

  case class Input(recipient: NodeId, feature: Double)
  case class WeightedInput(recipient: NodeId, feature: Double, weight: Double)

  case class AddInputs(recipient: NodeId, input: Seq[NodeId])
  case class AddOutputs(recipient: NodeId, output: Seq[NodeId])
  case object Ack

  val idExtractor: ShardRegion.IdExtractor = {
    case i: AddInputs => (i.recipient.toString, i)
    case o: AddOutputs => (o.recipient.toString, o)
    case s: WeightedInput => (s.recipient.toString, s)
    case s: Input => (s.recipient.toString, s)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case i: AddInputs => (i.recipient.hashCode % 100).toString
    case o: AddOutputs => (o.recipient.hashCode % 100).toString
    case s: WeightedInput => (s.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
  }
}

trait Node extends Actor

trait HasInputs extends Node {
  var inputs: Seq[NodeId] = Seq()
  def addInput(): Receive = {
    case AddInputs(_, i) =>
      inputs = i
      sender() ! Ack
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[NodeId] = Seq()
  def addOutput(): Receive = {
    case AddOutputs(_, o) =>
      outputs = o
      sender() ! Ack
  }
}