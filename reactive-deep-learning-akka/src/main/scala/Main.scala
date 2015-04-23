import Node.{Input, AddInput, AddOutput}
import akka.actor.{Props, ActorSystem}

object Main extends App {

  override def main(params: Array[String]) = {
    val system = ActorSystem("test")

    val inputLayer1 = system.actorOf(Props[InputNode])
    val inputLayer2 = system.actorOf(Props[InputNode])
    val inputLayer3 = system.actorOf(Props[InputNode])

    val hiddenLayer1 = system.actorOf(Props[Perceptron])
    val hiddenLayer2 = system.actorOf(Props[Perceptron])

    val outputLayer = system.actorOf(Props[OutputNode])

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
    inputLayer1 ! AddOutput(Seq(hiddenLayer1))
    inputLayer1 ! AddOutput(Seq(hiddenLayer2))
    inputLayer2 ! AddOutput(Seq(hiddenLayer1))
    inputLayer2 ! AddOutput(Seq(hiddenLayer2))
    inputLayer3 ! AddOutput(Seq(hiddenLayer1))
    inputLayer3 ! AddOutput(Seq(hiddenLayer2))

    hiddenLayer1 ! AddInput(Seq(inputLayer1))
    hiddenLayer1 ! AddInput(Seq(inputLayer2))
    hiddenLayer1 ! AddInput(Seq(inputLayer3))
    hiddenLayer1 ! AddOutput(Seq(outputLayer))

    hiddenLayer2 ! AddInput(Seq(inputLayer1))
    hiddenLayer2 ! AddInput(Seq(inputLayer2))
    hiddenLayer2 ! AddInput(Seq(inputLayer3))
    hiddenLayer2 ! AddOutput(Seq(outputLayer))

    outputLayer ! AddInput(Seq(hiddenLayer1))
    outputLayer ! AddInput(Seq(hiddenLayer2))

    while(true) {
      val in1 = scala.io.StdIn.readLine().toDouble
      val in2 = scala.io.StdIn.readLine().toDouble
      val in3 = scala.io.StdIn.readLine().toDouble

      inputLayer1 ! in1
      inputLayer2 ! in2
      inputLayer3 ! in3
    }
  }

}
