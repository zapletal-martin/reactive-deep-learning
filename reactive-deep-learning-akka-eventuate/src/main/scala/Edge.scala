import Node.InputCommand
import Node.WeightedInputCommand
import Node._
import Edge.{AddInputCommand, AddOutputCommand}
import akka.actor.{ActorRef, Actor, Props}
import com.rbmhtechnology.eventuate.EventsourcedActor

import scala.util.{Success, Failure}

object Edge {
  case class AddInputCommand(input: ActorRef)
  case class AddOutputCommand(output: ActorRef)

  def props(aggregateId: Option[String], replicaId: String, eventLog: ActorRef): Props =
    Props(new Edge(aggregateId, replicaId, eventLog))
}

trait HasInput extends Actor {
  var input: ActorRef = _

  def addInput(): Receive = {
    case AddInputCommand(i) =>
      input = i
      sender() ! Ack
  }
}

trait HasOutput extends Actor {
  var output: ActorRef = _

  def addOutput(): Receive = {
    case AddOutputCommand(o) =>
      output = o
      sender() ! Ack
  }
}

class Edge(
    override val aggregateId: Option[String],
    override val replicaId: String,
    override val eventLog: ActorRef) extends EventsourcedActor with HasInput with HasOutput {

  var weight: Double = 0.3
  var count: Int = 1

  override def onCommand: Receive = run orElse addInput orElse addOutput

  override def onEvent: Receive = {
    case UpdatedWeightEvent(w) =>
      weight = (weight * count + w) / (count + 1)
      count = count + 1
  }

  def run: Receive = {
    case InputCommand(f) =>
      output ! WeightedInputCommand(f, weight)
      println(s"AggregateId $aggregateId replicaId $replicaId has weight $weight")
    case UpdateWeightCommand(w) =>
      persist(UpdatedWeightEvent(w)) {
        case Success(evt) =>
          onEvent(evt)
          println(s"Successfuly persisted weight update $evt")
        case Failure(e) =>
          println(s"Failed to persist weight update ${e.getMessage}")
      }
  }
}
