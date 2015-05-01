import Node.{NodeId, Input, WeightedInput}
import akka.actor.Actor
import akka.contrib.pattern.{ShardRegion, ClusterSharding}

object Edge {
  val idExtractor: ShardRegion.IdExtractor = {
    case s: Input => (s.recipient.toString, s)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case s: Input => (s.recipient.hashCode % 100).toString
  }

  val shardName: String = "Edge"
}

class Edge(val in: NodeId, val out: NodeId) extends Actor {
  var weight: Double = 0.3

  override def receive: Receive = {
    case Input(r, f) =>
      ClusterSharding(context.system).shardRegion(Perceptron.shardName) ! WeightedInput(out, f, weight)
  }
}
