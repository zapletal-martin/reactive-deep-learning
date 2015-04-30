import Node.Input
import akka.contrib.pattern.ClusterSharding

class InputNode() extends HasOutputs {
  override def receive = run orElse addOutput

  def run: Receive = {
    case Input(_, f) =>
      val shardRegion = ClusterSharding(context.system).shardRegion(Perceptron.shardName)
      outputs.foreach(shardRegion ! Input(_, f))
  }
}