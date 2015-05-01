import Edge.AddInput
import Node.{NodeId, Input, WeightedInput}
import akka.actor.Actor
import akka.contrib.pattern.{ShardRegion, ClusterSharding}

object Edge {
  case class AddInput(recipient: NodeId, input: NodeId)
  case class AddOutput(recipient: NodeId, output: NodeId)

  val idExtractor: ShardRegion.IdExtractor = {
    case a: AddInput => (a.recipient.toString, a)
    case o: AddOutput => (o.recipient.toString, o)
    case s: Input => (s.recipient.toString, s)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case a: AddInput => (a.recipient.hashCode % 100).toString
    case o: AddOutput => (o.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
  }

  val shardName: String = "Edge"
}

trait HasInput extends Actor {
  var input: NodeId = _
  def addInput(): Receive = { case AddInput(_, i) => input = i }
}

trait HasOutput extends Actor {
  var output: NodeId = _
  def addOutput(): Receive = { case AddInput(_, o) => output = o }
}

class Edge(val in: NodeId, val out: NodeId) extends HasInput with HasOutput {
  var weight: Double = 0.3

  override def receive: Receive = run orElse addInput orElse addOutput

  def run: Receive = {
    case Input(r, f) =>
      ClusterSharding(context.system).shardRegion(Perceptron.shardName) ! WeightedInput(out, f, weight)
  }
}
