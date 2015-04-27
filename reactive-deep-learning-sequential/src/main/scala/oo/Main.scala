package oo

import oo.Node.WeightedInput

object Main extends App {
  override def main (args: Array[String]) {
    val inputLayer1 = new InputNode()
    val inputLayer2 = new InputNode()
    val inputLayer3 = new InputNode()

    //Hidden layer nodes.
    val hiddenLayer1 = new Perceptron()
    val hiddenLayer2 = new Perceptron()

    //Output layer nodes.
    val outputLayer = new OutputNode()

    //Input layer to hidden layer edges.
    val edgei1h1 = new Edge(inputLayer1, hiddenLayer1)
    val edgei1h2 = new Edge(inputLayer1, hiddenLayer2)
    val edgei2h1 = new Edge(inputLayer2, hiddenLayer1)
    val edgei2h2 = new Edge(inputLayer2, hiddenLayer2)
    val edgei3h1 = new Edge(inputLayer3, hiddenLayer1)
    val edgei3h2 = new Edge(inputLayer3, hiddenLayer2)

    //Hidden layer to output layer edges.
    val edgeh1o1 = new Edge(hiddenLayer1, outputLayer)
    val edgeh2o1 = new Edge(hiddenLayer2, outputLayer)

    //Linking edges to nodes.
    inputLayer1.addOutput(Seq(edgei1h1, edgei1h2))
    inputLayer2.addOutput(Seq(edgei2h1, edgei2h2))
    inputLayer3.addOutput(Seq(edgei3h1, edgei1h2))

    hiddenLayer1.addInput(Seq(edgei1h1, edgei2h1, edgei3h1))
    hiddenLayer1.addOutput(Seq(edgeh1o1))

    hiddenLayer2.addInput(Seq(edgei1h2, edgei2h2, edgei3h2))
    hiddenLayer2.addOutput(Seq(edgeh2o1))

    outputLayer.addInput(Seq(edgeh1o1, edgeh2o1))

    scala.io.Source.fromFile("src/main/resources/data.csv")
      .getLines()
      .foreach{ l =>
      val splits = l.split(",")

      inputLayer1.run(WeightedInput(splits(0).toDouble, 1))
      inputLayer2.run(WeightedInput(splits(1).toDouble, 1))
      inputLayer3.run(WeightedInput(splits(2).toDouble, 1))
    }
  }
}
