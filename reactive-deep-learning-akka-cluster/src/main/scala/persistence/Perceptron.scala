package persistence

import akka.actor.Props
import akka.contrib.pattern.ClusterSharding
import persistence.Node._

object Perceptron {
  def props(): Props = Props[Perceptron]
  val shardName: String = "Perceptron"
}

class Perceptron() extends Neuron {
  override def persistenceId: String = self.path.name

  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.2

  var weightsT: Vector[Double] = Vector()
  var featuresT: Vector[Double] = Vector()

  override def receiveCommand: Receive = run orElse addInput orElse addOutput

  override def receiveRecover: Receive = recover orElse addInputRecover orElse addOutputRecover

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

    case UpdateBiasCommand(r, b) =>
      persist(UpdatedBiasEvent(r, b)) { event =>
        bias = event.bias
      }
  }

  def recover: Receive = {
    case UpdatedBiasEvent(_, b) =>
      bias = b
  }
}