package persistence

import akka.actor.Props
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.persistence.PersistentActor
import persistence.Edge.{UpdatedWeightEvent, UpdateWeightCommand}
import persistence.Node.Input
import persistence.Node.WeightedInput
import persistence.Node._
import persistence.{OutputNode, Perceptron}

object Persistence {

}

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

object Edge {
  case class AddInputCommand(recipient: NodeId, input: NodeId)
  case class AddOutputCommand(recipient: NodeId, output: NodeId)
  case class UpdateWeightCommand(recipient: NodeId, weight: Double)

  case class AddedInputEvent(recipient: NodeId, input: NodeId)
  case class AddedOutputEvent(recipient: NodeId, output: NodeId)
  case class UpdatedWeightEvent(recipient: NodeId, weight: Double)


  val idExtractor: ShardRegion.IdExtractor = {
    case a: AddInputCommand => (a.recipient.toString, a)
    case o: AddOutputCommand => (o.recipient.toString, o)
    case s: Input => (s.recipient.toString, s)
    case w: UpdateWeightCommand => (w.recipient, w)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case a: AddInputCommand => (a.recipient.hashCode % 100).toString
    case o: AddOutputCommand => (o.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
    case w: UpdateWeightCommand => (w.recipient.hashCode % 100).toString
  }

  def props(): Props = Props[Edge]
  val shardName: String = "Edge"
}

trait HasInput extends PersistentActor {
  var input: NodeId = _
  def addInput(): Receive = {
    case AddInputCommand(r, i) =>
      persist(AddedInputEvent(r, i)) { event =>
        input = event.input
        sender() ! Ack
      }
  }

  def addInputRecover(): Receive = {
    case AddedInputEvent(_, i) => {
      println(s"Recovering AddedInputEvent in $persistenceId")
      input = i
    }
  }
}

trait HasOutput extends PersistentActor {
  var output: NodeId = _
  def addOutput(): Receive = {
    case AddOutputCommand(r, o) =>
      persist(AddedOutputEvent(r, o)) { event =>
        output = event.output
        sender() ! Ack
      }
  }

  def addOutputRecover(): Receive = {
    case  AddedOutputEvent(_, o) => {
      println(s"Recovering AddedOutputEvent in $persistenceId")
      output = o
    }
  }
}

object Perceptron {
  val shardName = "Perceptron"
}

class Edge extends PersistentActor with HasInput with HasOutput {
  override def persistenceId: String = self.path.name
  var weight: Double = 0.3

  override def receiveCommand: Receive = run orElse addInput orElse addOutput
  override def receiveRecover: Receive = recover orElse addInputRecover orElse addOutputRecover

  val shardRegion = ClusterSharding(context.system).shardRegion(Perceptron.shardName)

  def run: Receive = {
    case Input(r, f) => shardRegion ! WeightedInput(output, f, weight)

    case UpdateWeightCommand(r, w) =>
      persist(UpdatedWeightEvent(r, w)) { event =>
        weight = event.weight
      }
  }

  def recover: Receive = {
    case UpdatedWeightEvent(_, w) =>
      weight = w
  }
}
