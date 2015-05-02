import Node.{Input, AddInputs, AddOutputs}
import Edge.{AddInput, AddOutput}
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {

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
  override def main(params: Array[String]) = {
    val system = ActorSystem("akka")

    implicit val t = Timeout(10 seconds)
    val d = t.duration

    //Input layer nodes.
    val inputLayer1 = system.actorOf(InputNode.props)
    val inputLayer2 = system.actorOf(InputNode.props)
    val inputLayer3 = system.actorOf(InputNode.props)

    //Hidden layer nodes.
    val hiddenLayer1 = system.actorOf(Perceptron.props)
    val hiddenLayer2 = system.actorOf(Perceptron.props)

    //Output layer nodes.
    val outputLayer = system.actorOf(Props[OutputNode])

    val edgei1h1 = system.actorOf(Edge.props)
    val edgei1h2 = system.actorOf(Edge.props)
    val edgei2h1 = system.actorOf(Edge.props)
    val edgei2h2 = system.actorOf(Edge.props)
    val edgei3h1 = system.actorOf(Edge.props)
    val edgei3h2 = system.actorOf(Edge.props)

    val edgeh1o1 = system.actorOf(Edge.props)
    val edgeh2o1 = system.actorOf(Edge.props)

    //Input layer to hidden layer edges.
    Await.result(edgei1h1 ? AddInput(inputLayer1), d)
    Await.result(edgei1h1 ? AddOutput(hiddenLayer1), d)

    Await.result(edgei1h2 ? AddInput(inputLayer1), d)
    Await.result(edgei1h2 ? AddOutput(hiddenLayer2), d)

    Await.result(edgei2h1 ? AddInput(inputLayer2), d)
    Await.result(edgei2h1 ? AddOutput(hiddenLayer1), d)

    Await.result(edgei2h2 ? AddInput(inputLayer2), d)
    Await.result(edgei2h2 ? AddOutput(hiddenLayer2), d)

    Await.result(edgei3h1 ? AddInput(inputLayer3), d)
    Await.result(edgei3h1 ? AddOutput(hiddenLayer1), d)

    Await.result(edgei3h2 ? AddInput(inputLayer3), d)
    Await.result(edgei3h2 ? AddOutput(hiddenLayer2), d)

    //Hidden layer to output layer edges.
    Await.result(edgeh1o1 ? AddInput(hiddenLayer1), d)
    Await.result(edgeh1o1 ? AddOutput(outputLayer), d)

    Await.result(edgeh2o1 ? AddInput(hiddenLayer2), d)
    Await.result(edgeh2o1 ? AddOutput(outputLayer), d)

    //Linking edges to nodes.
    Await.result(inputLayer1 ? AddOutputs(Seq(edgei1h1, edgei1h2)), d)
    Await.result(inputLayer2 ? AddOutputs(Seq(edgei2h1, edgei2h2)), d)
    Await.result(inputLayer3 ? AddOutputs(Seq(edgei3h1, edgei1h2)), d)

    Await.result(hiddenLayer1 ? AddInputs(Seq(edgei1h1, edgei2h1, edgei3h1)), d)
    Await.result(hiddenLayer1 ? AddOutputs(Seq(edgeh1o1)), d)

    Await.result(hiddenLayer2 ? AddInputs(Seq(edgei1h2, edgei2h2, edgei3h2)), d)
    Await.result(hiddenLayer2 ? AddOutputs(Seq(edgeh2o1)), d)

    Await.result(outputLayer ? AddInputs(Seq(edgeh1o1, edgeh2o1)), d)

    scala.io.Source.fromFile("src/main/resources/data.csv")
      .getLines()
      .foreach{ l =>
        val splits = l.split(",")

        inputLayer1 ! Input(splits(0).toDouble)
        inputLayer2 ! Input(splits(1).toDouble)
        inputLayer3 ! Input(splits(2).toDouble)
      }
  }
}
