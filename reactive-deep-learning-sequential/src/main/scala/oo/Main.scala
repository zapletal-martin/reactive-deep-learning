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
    val outputLayer = new Perceptron()

    //Printer
    val printer = new OutputNode()

    val edgei1h1 = new Edge()
    val edgei1h2 = new Edge()
    val edgei2h1 = new Edge()
    val edgei2h2 = new Edge()
    val edgei3h1 = new Edge()
    val edgei3h2 = new Edge()
    val edgeh1o1 = new Edge()
    val edgeh2o1 = new Edge()
    val edgeo1p1 = new Edge()

    //Input layer to hidden layer edges.
    edgei1h1.addInput(inputLayer1)
    edgei1h1.addOutput(hiddenLayer1)

    edgei1h2.addInput(inputLayer1)
    edgei1h2.addOutput(hiddenLayer2)

    edgei2h1.addInput(inputLayer2)
    edgei2h1.addOutput(hiddenLayer1)

    edgei2h2.addInput(inputLayer2)
    edgei2h2.addOutput(hiddenLayer2)

    edgei3h1.addInput(inputLayer3)
    edgei3h1.addOutput(hiddenLayer1)

    edgei3h2.addInput(inputLayer3)
    edgei3h2.addOutput(hiddenLayer2)

    //Hidden layer to output layer edges.
    edgeh1o1.addInput(hiddenLayer1)
    edgeh1o1.addOutput(outputLayer)

    edgeh2o1.addInput(hiddenLayer2)
    edgeh2o1.addOutput(outputLayer)

    //Output layer to printer
    edgeo1p1.addInput(outputLayer)
    edgeo1p1.addOutput(printer)

    //Linking edges to nodes.
    inputLayer1.addOutputs(Seq(edgei1h1, edgei1h2))
    inputLayer2.addOutputs(Seq(edgei2h1, edgei2h2))
    inputLayer3.addOutputs(Seq(edgei3h1, edgei1h2))

    hiddenLayer1.addInputs(Seq(edgei1h1, edgei2h1, edgei3h1))
    hiddenLayer1.addOutputs(Seq(edgeh1o1))

    hiddenLayer2.addInputs(Seq(edgei1h2, edgei2h2, edgei3h2))
    hiddenLayer2.addOutputs(Seq(edgeh2o1))

    outputLayer.addInputs(Seq(edgeh1o1, edgeh2o1))
    outputLayer.addOutputs(Seq(edgeo1p1))

    printer.addInputs(Seq(edgeo1p1))

    scala.io.Source.fromFile("src/main/resources/data.csv")
      .getLines()
      .toList
      .par
      .foreach{ l =>
      val splits = l.split(",")

      inputLayer1.run(WeightedInput(splits(0).toDouble, 1))
      inputLayer2.run(WeightedInput(splits(1).toDouble, 1))
      inputLayer3.run(WeightedInput(splits(2).toDouble, 1))
    }
  }
}
