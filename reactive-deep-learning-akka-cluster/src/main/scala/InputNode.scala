import Node.Input
import akka.contrib.pattern.ClusterSharding

object InputNode {
  val shardName: String = "InputNode"
}

abstract class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case Input(_, f) =>
      val shardRegion = ClusterSharding(context.system).shardRegion(Edge.shardName)
      outputs.foreach(shardRegion ! Input(_, f))
  }
}