import Node.{WeightedInput, Input}
import akka.actor.{ActorRef, Actor}

class Edge(val in: ActorRef, val out: ActorRef) extends Actor {
  var weight: Double = 0.3

  override def receive: Receive = {
    case Input(f) => out ! WeightedInput(f, weight)
  }
}
