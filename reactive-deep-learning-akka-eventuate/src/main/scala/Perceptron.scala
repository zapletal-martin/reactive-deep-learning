import Node._
import akka.actor.{ActorRef, Props}
import com.rbmhtechnology.eventuate.EventsourcedActor

import scala.util.{Failure, Success}

object Perceptron {
  def props(aggregateId: Option[String], replicaId: String, eventLog: ActorRef): Props =
    Props(new Perceptron(aggregateId, replicaId, eventLog))
}

class Perceptron(
    override val aggregateId: Option[String],
    override val replicaId: String,
    override val eventLog: ActorRef) extends EventsourcedActor with Neuron {

  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.2

  var weightsT: Vector[Double] = Vector()
  var featuresT: Vector[Double] = Vector()

  override def onCommand: Receive = run orElse addInput orElse addOutput

  //TODO: Handle conflicts
  override def onEvent: Receive = {
    case UpdatedBiasEvent(b) => bias = b
  }

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[ActorRef]) =
    w.length == in.length && f.length == in.length

  def run: Receive = {
    case WeightedInputCommand(f, w) =>
      featuresT = featuresT :+ f
      weightsT = weightsT :+ w

      if(allInputsAvailable(weightsT, featuresT, inputs)) {
        val activation = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

        featuresT = Vector()
        weightsT = Vector()

        outputs.foreach(_ ! InputCommand(activation))
      }

    case UpdateBiasCommand(b) =>
      persist(UpdatedBiasEvent(b)) {
        case Success(evt) =>
          onEvent(evt)
        case Failure(e) =>
      }
  }
}