import Node.Input
import Node.WeightedInput
import Node._
import Edge.{AddInput, AddOutput}
import akka.actor.{ActorRef, Actor, Props}

object Edge {
  case class AddInput(input: ActorRef)
  case class AddOutput(output: ActorRef)

  def props(): Props = Props[Edge]
}

trait HasInput extends Actor {
  var input: ActorRef = _

  def addInput(): Receive = {
    case AddInput(i) =>
      input = i
      sender() ! Ack
  }
}

trait HasOutput extends Actor {
  var output: ActorRef = _

  def addOutput(): Receive = {
    case AddOutput(o) =>
      output = o
      sender() ! Ack
  }
}

class Edge extends HasInput with HasOutput {
  var weight: Double = 0.3

  override def receive: Receive = run orElse addInput orElse addOutput

  def run: Receive = {
    case Input(f) => output ! WeightedInput(f, weight)
  }
}
