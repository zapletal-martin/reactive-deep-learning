package persistence

import akka.actor.Props
import akka.contrib.pattern.ClusterSharding
import persistence.Node.Input

object InputNode {
  def props(): Props = Props[InputNode]
  val shardName: String = "InputNode"
}

class InputNode() extends HasOutputs {
  override def persistenceId: String = self.path.name

  override def receiveCommand: Receive = run orElse addOutput

  override def receiveRecover: Receive = {
    case _ => println(s"Recovering $persistenceId")
  }

  val shardRegion = ClusterSharding(context.system).shardRegion(Edge.shardName)

  def run: Receive = {
    case Input(_, f) => outputs.foreach(shardRegion ! Input(_, f))
  }


}