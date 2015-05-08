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

  var weightsT: Seq[Double] = Seq()
  var featuresT: Seq[Double] = Seq()

  override def receiveCommand: Receive = run orElse addInput orElse addOutput

  override def receiveRecover: Receive = {
    case _ => println(s"Recovering $persistenceId")
  }

  private def allInputsAvailable(w: Seq[Double], f: Seq[Double], in: Seq[NodeId]) =
    w.length == in.length && f.length == in.length

  val shardRegion = ClusterSharding(context.system).shardRegion(Edge.shardName)

  def run: Receive = {
    case WeightedInput(_, f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(allInputsAvailable(weightsT, featuresT, inputs)) {
        val activation = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Seq()
        weightsT = Seq()

        outputs.foreach(shardRegion ! Input(_, activation))
      }
  }
}