package persistence

import akka.actor.Props
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.persistence.PersistentActor
import persistence.Edge._
import persistence.Node.{Ack, Input, NodeId, WeightedInput}

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
    case AddedInputEvent(_, i) =>
      input = i
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
    case AddedOutputEvent(_, o) =>
      output = o
  }
}

class Edge extends HasInput with HasOutput {
  override def persistenceId: String = self.path.name

  var weight: Double = 0.3

  override def receiveCommand: Receive = run orElse addInput orElse addOutput

  override def receiveRecover: Receive = recover orElse addInputRecover orElse addOutputRecover

  val shardRegion = ClusterSharding(context.system).shardRegion(Perceptron.shardName)
  val shardRegionLastLayer = ClusterSharding(context.system).shardRegion(OutputNode.shardName)

  def run: Receive = {
    case Input(r, f) =>
      if(output.head == 'p')
        shardRegionLastLayer ! WeightedInput(output, f, weight)
      else
        shardRegion ! WeightedInput(output, f, weight)

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