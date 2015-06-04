package oo

import oo.Node.{Input, WeightedInput}

import scala.io.Source
import scala.math._

object Oo {
  val edgei2h1 = new Edge()
  val edgei3h1 = new Edge()
  val edgeh1o1 = new Edge()
  val inputLayer1 = new InputNode()
  val inputLayer2 = new InputNode()
  val inputLayer3 = new InputNode()


  val hiddenLayer1 = new Perceptron()
  val edgei1h1 = new Edge()
  edgei1h1.addInput(inputLayer1)
  edgei1h1.addOutput(hiddenLayer1)
  hiddenLayer1.addInputs(Seq(edgei1h1, edgei2h1, edgei3h1))
  hiddenLayer1.addOutputs(Seq(edgeh1o1))

  Source.fromFile("src/main/resources/data2.csv")
    .getLines()
    .toList
    .par
    .foreach { l =>
      ...
    }

  Source.fromFile("src/main/resources/data2.csv")
    .getLines()
    .foreach{ l =>
      val splits = l.split(",")

      inputLayer1.run(
        WeightedInput(splits(0).toDouble, 1))

      inputLayer2.run(
        WeightedInput(splits(1).toDouble, 1))

      inputLayer3.run(
        WeightedInput(splits(2).toDouble, 1))
    }
}

class InputNode extends HasOutputs {
  override def run(in: WeightedInput): Unit = outputs.foreach(_.run(Input(in.feature)))
}

object Node {
  case class Input(feature: Double)
  case class WeightedInput(feature: Double, weight: Double)
}

trait Node {
  def run(in: WeightedInput)
}

trait HasInput {
  var input: Node = _
  def addInput(i: Node): Unit = input = i
}

trait HasOutput {
  var output: Node = _
  def addOutput(o: Node): Unit = output = o
}

class Edge extends HasInput with HasOutput {
  var weight: Double = 0.3
  def run(in: Input) = output.run(WeightedInput(in.feature, weight))
}

trait HasInputs extends Node {
  var inputs: Seq[Edge] = Seq()
  def addInputs(i: Seq[Edge]): Unit = inputs = i
}

trait HasOutputs extends Node {
  var outputs: Seq[Edge] = Seq()
  def addOutputs(i: Seq[Edge]): Unit = outputs = i
}

object Neuron {
  val simple: Double => Double = x => if(x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}

trait Neuron extends HasInputs with HasOutputs {
  var bias: Double
  var activationFunction: Double => Double
}

class Perceptron extends Neuron {
  override var activationFunction: Double => Double = Neuron.sigmoid
  override var bias: Double = 0.2

  var inputs: Seq[Edge] = Seq()
  var outputs: Seq[Edge] = Seq()

  var weightsT: Seq[Double] = Vector()
  var featuresT: Seq[Double] = Vector()

  private def allInputsAvailable(w: Seq[Double], f: Seq[Double], in: Seq[Edge]) =
    w.length == in.length && f.length == in.length

  override def run(in: WeightedInput): Unit = {
    featuresT = featuresT :+ in.feature
    weightsT = weightsT :+ in.weight

    if(allInputsAvailable(weightsT, featuresT, inputs)) {
      val activation = activationFunction(weightsT.zip(featuresT).map(x => x._1 * x._2).sum + bias)

      featuresT = Vector()
      weightsT = Vector()

      outputs.foreach(_.run(Input(activation)))
    }
  }
}

