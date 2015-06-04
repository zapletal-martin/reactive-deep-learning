package crdt

import _root_.replication.Node.Input
import _root_.replication.Node.WeightedInput
import akka.actor.{Props, ActorRef, Actor}
import akka.cluster.Cluster
import akka.contrib.datareplication.{DataReplication, GCounter}
import akka.contrib.datareplication.Replicator.{Changed, WriteLocal, Update, Subscribe}
import crdt.Edge.{UpdateWeight, AddOutput, AddInput}
import crdt.Node.{WeightedInput, Input}

object Crdt {

}

object Node {
  type NodeId = String

  case class Input(feature: Double)
  case class WeightedInput(feature: Double, weight: Double)

  case class AddInputs(input: Seq[ActorRef])
  case class AddOutputs(output: Seq[ActorRef])

  case class UpdateBias(bias: Double)

  case object Ack
}

object Edge {
  case class AddInput(input: ActorRef)
  case class AddOutput(output: ActorRef)
  case class UpdateWeight(weight: Long)

  def props(): Props = Props[Edge]
}


trait HasInput extends Actor {
  var input: ActorRef = _
  def addInput(): Receive = {
    case AddInput(i) =>
      input = i
      sender() ! Ack
  }
}

trait HasOutput extends Actor {
  var output: ActorRef = _
  def addOutput(): Receive = {
    case AddOutput(o) =>
      output = o
      sender() ! Ack
  }
}

class Edge extends Actor with HasInput with HasOutput {
  var weight: Double = 0.3

  val replicator = DataReplication(context.system).replicator
  implicit val cluster = Cluster(context.system)

  replicator ! Subscribe(self.path.name, self)

  override def receive: Receive = run orElse addInput orElse addOutput

  def run: Receive = {
    ...

    case UpdateWeight(w) =>
      replicator ! Update(self.path.name, GCounter(), WriteLocal)(_ + w)

    case Changed(key, GCounter(mergedWeight)) if key == self.path.name =>
      weight = mergedWeight
  }
}