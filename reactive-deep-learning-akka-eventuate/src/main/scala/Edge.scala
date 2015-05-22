import Node.InputCommand
import Node.WeightedInputCommand
import Node._
import Edge.{UpdateWeightCommand, UpdatedWeightEvent, AddInputCommand, AddOutputCommand}
import akka.actor.{ActorRef, Actor, Props}
import com.rbmhtechnology.eventuate.{VectorTime, ConcurrentVersions, EventsourcedActor}

import scala.util.{Success, Failure}

object Edge {
  case class AddInputCommand(input: ActorRef)
  case class AddOutputCommand(output: ActorRef)

  case class UpdateWeightCommand(weight: Double)
  case class UpdatedWeightEvent(weight: Double)

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

  override def onCommand: Receive = run orElse addInput orElse addOutput

  private var versionedState: ConcurrentVersions[Double, Double] =
    ConcurrentVersions(0.3, (s, a) => a)

  override def onEvent: Receive = {
    case UpdatedWeightEvent(w) =>
      versionedState = versionedState.update(w, lastVectorTimestamp, lastEmitterReplicaId)
      if (versionedState.conflict) {
        println(s"Conflicting versions on replica $replicaId " + versionedState.all.map(v => s"value ${v.value} vector clock ${v.updateTimestamp} emitted by replica ${v.emitterReplicaId}"))
        val conflictingVersions = versionedState.all
        val avg = conflictingVersions.map(_.value).sum / conflictingVersions.size

        val newTimestamp = conflictingVersions.map(_.updateTimestamp).foldLeft(VectorTime())(_.merge(_))
        versionedState.update(avg, newTimestamp, replicaId)
        versionedState = versionedState.resolve(newTimestamp)

        weight = versionedState.all.head.value
        println(s"Conflicting versions on replica $replicaId resolved " + versionedState.all.map(v => s"value ${v.value} vector clock ${v.updateTimestamp}"))
      } else {
        weight = versionedState.all.head.value
      }
  }

  def run: Receive = {
    case InputCommand(f) =>
      output ! WeightedInputCommand(f, weight)
      //println(s"AggregateId $aggregateId replicaId $replicaId has weight $weight")
    case UpdateWeightCommand(w) =>
      persist(UpdatedWeightEvent(w)) {
        case Success(evt) => onEvent(evt)
        case Failure(e) =>
      }
  }
}
