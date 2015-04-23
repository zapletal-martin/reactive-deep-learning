import akka.actor.ActorRef
import scala.math.exp

object Neuron {
  val simple: Double => Double = x => if(x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}

trait Neuron extends HasInputs with HasOutputs {
  override var inputNodes: Seq[ActorRef] = Seq()
  override var outputNodes: Seq[ActorRef] = Seq()

  var weight: Double
  var bias: Double
  var activationFunction: Double => Double
}