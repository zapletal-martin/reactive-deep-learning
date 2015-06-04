package cluster

import Node.Input
import Node.UpdateBias
import Node.WeightedInput
import Node._
import akka.actor.{Actor, Props}
import akka.contrib.pattern.{ClusterSharding, ShardRegion}

import scala.math._

object Cluster {

}

object Node {
  type NodeId = String

  case class Input(recipient: NodeId, feature: Double)
  case class WeightedInput(recipient: NodeId, feature: Double, weight: Double)

  case class AddInputs(recipient: NodeId, input: Seq[NodeId])
  case class AddOutputs(recipient: NodeId, output: Seq[NodeId])

  case class UpdateBias(recipient: NodeId, bias: Double)

  case object Ack

  val idExtractor: ShardRegion.IdExtractor = {
    case i: AddInputs => (i.recipient.toString, i)
    case o: AddOutputs => (o.recipient.toString, o)
    case s: WeightedInput => (s.recipient.toString, s)
    case s: Input => (s.recipient.toString, s)
    case b: UpdateBias => (b.recipient.toString, b)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case i: AddInputs => (i.recipient.hashCode % 100).toString
    case o: AddOutputs => (o.recipient.hashCode % 100).toString
    case s: WeightedInput => (s.recipient.hashCode % 100).toString
    case s: Input => (s.recipient.hashCode % 100).toString
    case b: UpdateBias => (b.recipient.hashCode % 100).toString
  }
}

trait Node extends Actor

trait HasInputs extends Node {
  var inputs: Seq[NodeId] = Seq()
  def addInput(): Receive = {
    case AddInputs(_, i) =>
      inputs = i
      sender() ! Ack
  }
}

trait HasOutputs extends Node {
  var outputs: Seq[NodeId] = Seq()
  def addOutput(): Receive = {
    case AddOutputs(_, o) =>
      outputs = o
      sender() ! Ack
  }
}

object Neuron {
  val simple: Double => Double = x => if(x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}

trait Neuron extends HasInputs with HasOutputs {
  var bias: Double
  var activationFunction: Double => Double
}

object Perceptron {
  def props(): Props = Props[Perceptron]
  val shardName: String = "Perceptron"
}

object Edge {
  val shardName = "Edge"
}

class Perceptron() extends Actor with Neuron {

  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.2

  var weightsT: Vector[Double] = Vector()
  var featuresT: Vector[Double] = Vector()

  override def receive = run orElse addInput orElse addOutput

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[NodeId]) =
    w.length == in.length && f.length == in.length

  val shardRegion = ClusterSharding(context.system).shardRegion(Edge.shardName)

  def run: Receive = {
    case WeightedInput(_, f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(allInputsAvailable(weightsT, featuresT, inputs)) {
        val activation = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Vector()
        weightsT = Vector()

        outputs.foreach(shardRegion ! Input(_, activation))
      }

    case UpdateBias(_, b) =>
      bias = b
  }
}