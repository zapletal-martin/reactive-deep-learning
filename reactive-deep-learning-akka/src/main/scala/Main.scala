import Node.{Input, AddInput, AddOutput}
import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.duration._

object Main extends App {

  override def main(params: Array[String]) = {
    val system = ActorSystem("test")

    //Input layer nodes.
    val inputLayer1 = system.actorOf(Props[InputNode])
    val inputLayer2 = system.actorOf(Props[InputNode])
    val inputLayer3 = system.actorOf(Props[InputNode])

    //Hidden layer nodes.
    val hiddenLayer1 = system.actorOf(Props[Perceptron])
    val hiddenLayer2 = system.actorOf(Props[Perceptron])

    //Output layer nodes.
    val outputLayer = system.actorOf(Props[OutputNode])

    //Input layer to hidden layer edges.
    val edgei1h1 = system.actorOf(Props(new Edge(inputLayer1, hiddenLayer1)))
    val edgei1h2 = system.actorOf(Props(new Edge(inputLayer1, hiddenLayer2)))
    val edgei2h1 = system.actorOf(Props(new Edge(inputLayer2, hiddenLayer1)))
    val edgei2h2 = system.actorOf(Props(new Edge(inputLayer2, hiddenLayer2)))
    val edgei3h1 = system.actorOf(Props(new Edge(inputLayer3, hiddenLayer1)))
    val edgei3h2 = system.actorOf(Props(new Edge(inputLayer3, hiddenLayer2)))

    //Hidden layer to output layer edges.
    val edgeh1o1 = system.actorOf(Props(new Edge(hiddenLayer1, outputLayer)))
    val edgeh2o1 = system.actorOf(Props(new Edge(hiddenLayer2, outputLayer)))

    //Linking edges to nodes.
    inputLayer1 ! AddOutput(Seq(edgei1h1, edgei1h2))
    inputLayer2 ! AddOutput(Seq(edgei2h1, edgei2h2))
    inputLayer3 ! AddOutput(Seq(edgei3h1, edgei1h2))

    hiddenLayer1 ! AddInput(Seq(edgei1h1, edgei2h1, edgei3h1))
    hiddenLayer1 ! AddOutput(Seq(edgeh1o1))

    hiddenLayer2 ! AddInput(Seq(edgei1h2, edgei2h2, edgei3h2))
    hiddenLayer2 ! AddOutput(Seq(edgeh2o1))

    outputLayer ! AddInput(Seq(edgeh1o1, edgeh2o1))

    //**************************************
    // I-----
    //  \    |
    //   ----H
    //  / \ / \
    // I   -   O
    //  \ / \ /
    //   ----H
    //  /    |
    // I-----
    //**************************************

    scala.io.Source.fromFile("src/main/resources/data.csv")
      .getLines()
      .foreach{ l =>
        val splits = l.split(",")

        inputLayer1 ! Input(splits(0).toDouble)
        inputLayer2 ! Input(splits(1).toDouble)
        inputLayer3 ! Input(splits(2).toDouble)
      }

    val reaper = system.actorOf(Props(new Actor {
      override def receive: Receive = {
        case _ => system.terminate()
      }
    }))

    import system.dispatcher
    system.scheduler.scheduleOnce(100 seconds, reaper, 'bye)
  }
}
