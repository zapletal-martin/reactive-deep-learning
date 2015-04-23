import akka.actor.ActorRef
import scala.math.exp

object Neuron {
  val simple: Double => Double = x => if(x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}

trait Neuron extends HasInputs with HasOutputs {
  override var inputs: Seq[ActorRef] = Seq()
  override var outputs: Seq[ActorRef] = Seq()

  var bias: Double
  var activationFunction: Double => Double
}