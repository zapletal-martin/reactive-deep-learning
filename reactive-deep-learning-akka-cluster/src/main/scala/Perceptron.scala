import Node._
import akka.actor.Props
import akka.contrib.pattern.ClusterSharding

object Perceptron {
  def props(): Props = Props[Perceptron]
  val shardName: String = "Perceptron"
}

class Perceptron() extends Neuron {

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