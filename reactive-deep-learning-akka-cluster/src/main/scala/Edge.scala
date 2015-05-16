import Edge.{UpdateWeight, AddOutput, AddInput}
import Node.{Ack, NodeId, Input, WeightedInput}
import akka.actor.{Props, Actor}
import akka.contrib.pattern.{ClusterSharding, ShardRegion}

object Edge {
  case class AddInput(recipient: NodeId, input: NodeId)
  case class AddOutput(recipient: NodeId, output: NodeId)
  case class UpdateWeight(recipient: NodeId, weight: Double)

  val idExtractor: ShardRegion.IdExtractor = {
    case a: AddInput => (a.recipient.toString, a)
    case o: AddOutput => (o.recipient.toString, o)
    case s: Input => (s.recipient.toString, s)
    case w: UpdateWeight => (w.recipient.toString, w)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case a: AddInput => (a.recipient.hashCode % 100).toString
    case o: AddOutput => (o.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
    case w: UpdateWeight => (w.recipient.hashCode % 100).toString
  }

  def props(): Props = Props[Edge]
  val shardName: String = "Edge"
}

trait HasInput extends Actor {
  var input: NodeId = _
  def addInput(): Receive = {
    case AddInput(_, i) =>
      input = i
      sender() ! Ack
  }
}

trait HasOutput extends Actor {
  var output: NodeId = _
  def addOutput(): Receive = {
    case AddOutput(_, o) =>
      output = o
      sender() ! Ack
  }
}

class Edge extends HasInput with HasOutput {
  var weight: Double = 0.3

  override def receive: Receive = run orElse addInput orElse addOutput

  val shardRegion = ClusterSharding(context.system).shardRegion(Perceptron.shardName)
  val shardRegionLastLayer = ClusterSharding(context.system).shardRegion(OutputNode.shardName)

  def run: Receive = {
    case Input(_, f) =>
      if(output.head == 'p')
        shardRegionLastLayer ! WeightedInput(output, f, weight)
      else
        shardRegion ! WeightedInput(output, f, weight)

    case UpdateWeight(_, w) =>
      weight = w
  }
}
