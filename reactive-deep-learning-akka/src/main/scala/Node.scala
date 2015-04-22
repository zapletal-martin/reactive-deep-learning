import akka.actor._

case class Input(features: Array[Double])
case object GetWeight
case class Weight(weight: Double)

case class AddInput(input: Seq[ActorRef])
case class AddOutput(output: Seq[ActorRef])

object Node {
  case class AddInputEdge(node: Node)
}

trait Node extends Actor { }

trait HasInputs extends Node {
  var inputNodes: Seq[ActorRef]
  def addInput: Receive = { case AddInput(i) => inputNodes = i }
}

trait HasOutputs extends Node {
  var outputNodes: Seq[ActorRef]
  def addOutput: Receive = { case AddOutput(o) => outputNodes = o }
}

object Neuron {
  val simpleActivation: Double => Double = x => if(x > 0.5) 1 else 0
}

trait Neuron extends HasInputs with HasOutputs {
  override var inputNodes: Seq[ActorRef] = Seq()
  override var outputNodes: Seq[ActorRef] = Seq()

  var weight: Double
  var bias: Double
  var activationFunction: Double => Double
}

class Perceptron() extends Neuron {

  override var weight: Double = _
  override var activationFunction: Double => Double = Neuron.simpleActivation
  override var bias: Double = _

  var weightsT: Seq[Double] = Seq()
  var featuresT: Seq[Double] = Seq()

  override def receive = run orElse addInput orElse addOutput

  def run: Receive = {
    case Input(f) =>
      assert(inputNodes.length == f.length)
      featuresT = f
      weightsT = Seq()
      inputNodes.foreach(_ ! GetWeight)

    case Weight(w) =>
      weightsT = weightsT :+ w

      if(weightsT.length == inputNodes.length) {
        val output = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)
        outputNodes.foreach(_ ! output)
      }

    case GetWeight => sender() ! Weight(weight)
  }
}

class InputNode() extends HasOutputs {
  override var outputNodes: Seq[ActorRef] = Seq()

  override def receive = run orElse addOutput

  def run: Actor.Receive = {
    case a: Input => outputNodes.foreach(_ ! a)
    case GetWeight => sender() ! Weight(1.0)
  }
}

class OutputNode() extends HasInputs {
  override var inputNodes: Seq[ActorRef] = Seq()

  override def receive = run orElse addInput

  def run: Receive = {
    case a: Double => println(s"Output: $a")
  }
}