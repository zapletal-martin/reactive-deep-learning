import Node.{NodeId, Input, WeightedInput}
import akka.actor.{Actor, ActorRef}
import akka.contrib.pattern.ClusterSharding

class Edge(val in: NodeId, val out: NodeId) extends Actor {
  var weight: Double = 0.3

  override def receive: Receive = {
    case Input(r, f) =>
      ClusterSharding(context.system).shardRegion(Perceptron.shardName) ! WeightedInput(out, f, weight)
  }
}
