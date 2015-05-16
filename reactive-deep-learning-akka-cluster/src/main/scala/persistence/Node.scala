package persistence

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.persistence.PersistentActor
import persistence.Node._

object Node {
  type NodeId = String

  case class Input(recipient: NodeId, feature: Double)
  case class WeightedInput(recipient: NodeId, feature: Double, weight: Double)

  case class AddInputsCommand(recipient: NodeId, input: Seq[NodeId])
  case class AddOutputsCommand(recipient: NodeId, output: Seq[NodeId])
  case class UpdateBiasCommand(recipient: NodeId, bias: Double)

  case class AddedInputsEvent(recipient: NodeId, input: Seq[NodeId])
  case class AddedOutputsEvent(recipient: NodeId, output: Seq[NodeId])
  case class UpdatedBiasEvent(recipient: NodeId, bias: Double)

  case object Ack

  val idExtractor: ShardRegion.IdExtractor = {
    case i: AddInputsCommand => (i.recipient.toString, i)
    case o: AddOutputsCommand => (o.recipient.toString, o)
    case s: WeightedInput => (s.recipient.toString, s)
    case s: Input => (s.recipient.toString, s)
    case b: UpdateBiasCommand => (b.recipient.toString, b)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case i: AddInputsCommand => (i.recipient.hashCode % 100).toString
    case o: AddOutputsCommand => (o.recipient.hashCode % 100).toString
    case s: WeightedInput => (s.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
    case b: UpdateBiasCommand => (b.recipient.hashCode % 100).toString
  }
}

trait Node extends PersistentActor

trait HasInputs extends Node {
  var inputs: Seq[NodeId] = Seq()
  def addInput(): Receive = {
    case AddInputsCommand(r, i) =>
      persist(AddedInputsEvent(r, i)) { event =>
        inputs = event.input
        sender() ! Ack
      }
  }

  def addInputRecover(): Receive = {
    case AddedInputsEvent(r, i) => {
      println(s"Recovering AddedInputsEvent in $persistenceId")
      inputs = i
    }
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[NodeId] = Seq()
  def addOutput(): Receive = {
    case AddOutputsCommand(r, o) =>
      persist(AddedOutputsEvent(r, o)) { event =>
        outputs = o
        sender() ! Ack
      }
  }

  def addOutputRecover(): Receive = {
    case AddedOutputsEvent(r, i) => {
      println(s"Recovering AddedOutputsEvent in $persistenceId")
      outputs = i
    }
  }
}