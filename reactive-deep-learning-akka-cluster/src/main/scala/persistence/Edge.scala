package persistence

import akka.actor.Props
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.persistence.PersistentActor
import akka.remote.Ack
import persistence.Edge.{AddedInputEvent, AddedOutputEvent, AddInputCommand, AddOutputCommand}
import persistence.Node.{Input, NodeId, WeightedInput}

object Edge {
  case class AddInputCommand(recipient: NodeId, input: NodeId)
  case class AddOutputCommand(recipient: NodeId, output: NodeId)

  case class AddedInputEvent(recipient: NodeId, input: NodeId)
  case class AddedOutputEvent(recipient: NodeId, output: NodeId)

  val idExtractor: ShardRegion.IdExtractor = {
    case a: AddInputCommand => (a.recipient.toString, a)
    case o: AddOutputCommand => (o.recipient.toString, o)
    case s: Input => (s.recipient.toString, s)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case a: AddInputCommand => (a.recipient.hashCode % 100).toString
    case o: AddOutputCommand => (o.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
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
}

class Edge extends HasInput with HasOutput {
  override def persistenceId: String = self.path.name

  var weight: Double = 0.3

  override def receiveCommand: Receive = run orElse addInput orElse addOutput

  override def receiveRecover: Receive = {
    case _ => println(s"Recovering $persistenceId")
  }

  val shardRegion = ClusterSharding(context.system).shardRegion(Perceptron.shardName)
  val shardRegionLastLayer = ClusterSharding(context.system).shardRegion(OutputNode.shardName)

  def run: Receive = {
    case Input(r, f) =>
      if(output.head == 'p')
        shardRegionLastLayer ! WeightedInput(output, f, weight)
      else
        shardRegion ! WeightedInput(output, f, weight)
  }



}
