import Node._
import akka.contrib.pattern.{ClusterSharding, ShardRegion}

object Perceptron {

  val idExtractor: ShardRegion.IdExtractor = {
    case a: AddInput => (a.recipient.toString, a)
    case s: WeightedInput => (s.feature.toString, s)
    case s: Input => {
      println("IDEXTRA")
      (s.recipient.toString, s)
    }
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case a: AddInput => (math.abs(a.recipient.hashCode) % 100).toString
    case s: WeightedInput => (math.abs(s.feature.hashCode) % 100).toString
    case s: Input => (math.abs(s.recipient.hashCode) % 100).toString
  }

  val shardName: String = "Perceptron"
}

class Perceptron() extends Neuron {

  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.1

  var weightsT: Seq[Double] = Seq()
  var featuresT: Seq[Double] = Seq()

  override def receive = run //orElse addInput orElse addOutput

  private def allInputsAvailable(w: Seq[Double], f: Seq[Double], in: Seq[NodeId]) =
    w.length == in.length && f.length == in.length

  def run: Receive = {
    case WeightedInput(_, f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(allInputsAvailable(weightsT, featuresT, inputs)) {
        val output = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Seq()
        weightsT = Seq()

        val shardRegion = ClusterSharding(context.system).shardRegion(Perceptron.shardName)
        outputs.foreach(shardRegion ! Input(_, output))
      }

    case a@_ => println(s"HOSE $a")
  }
}