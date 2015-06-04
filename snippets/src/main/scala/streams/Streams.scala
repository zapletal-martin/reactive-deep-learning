package streams

import breeze.linalg.{*, DenseMatrix}
import akka.stream.{FanInShape2, FlowShape, Outlet}
import akka.stream.scaladsl.{Source, Zip, Flow, FlowGraph}
import breeze.numerics.sigmoid

object Streams {
  def hiddenLayerWeights(topology: Array[Int], layer: Int, weights: DenseMatrix[Double]) =
    Source(() => Iterator.continually(
      new DenseMatrix[Double](topology(layer), topology(layer - 1), Array.fill(topology(layer) * topology(layer - 1))(0.3))))

  def hiddenLayer(layer: Int) = {
    def feedForward(features: DenseMatrix[Double], weightMatrices: DenseMatrix[Double]) = {
      val bias = 0.2
      val activation: DenseMatrix[Double] = weightMatrices * features
      activation(::, *) :+= bias
      sigmoid.inPlace(activation)
      activation
    }

    FlowGraph.partial() { implicit builder: FlowGraph.Builder[Unit] =>
      import akka.stream.scaladsl.FlowGraph.Implicits._

      val zipInputAndWeights = builder.add(Zip[DenseMatrix[Double], DenseMatrix[Double]]())
      val feedForwardFlow = builder.add(Flow[(DenseMatrix[Double], DenseMatrix[Double])]
        .map(x => feedForward(x._1, x._2)))

      zipInputAndWeights.out ~> feedForwardFlow

      new FanInShape2(zipInputAndWeights.in0, zipInputAndWeights.in1, feedForwardFlow.outlet)
    }
  }

  def network(topology: Array[Int], weights: DenseMatrix[Double]) = {

    FlowGraph.partial() { implicit builder: FlowGraph.Builder[Unit] =>
      import akka.stream.scaladsl.FlowGraph.Implicits._

      def buildLayer(
          layer: Int,
          input: Outlet[DenseMatrix[Double]],
          topology: Array[Int],
          weights: DenseMatrix[Double]): Outlet[DenseMatrix[Double]] = {

        val currentLayer = builder.add(hiddenLayer(layer))

        input                                        ~> currentLayer.in0
        hiddenLayerWeights(topology, layer, weights) ~> currentLayer.in1

        if (layer < topology.length - 1) buildLayer(layer + 1, currentLayer.out, topology, weights)
        else currentLayer.out
      }

      val flow = builder.add(Flow[DenseMatrix[Double]])
      val network = buildLayer(1, flow.outlet, topology, weights)

      new FlowShape(flow.inlet, network)
    }
  }
}

