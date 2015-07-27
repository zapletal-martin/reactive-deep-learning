import java.text.SimpleDateFormat
import java.util.Date

import akka.stream.scaladsl._
import akka.stream.{FanInShape2, FlowShape, Outlet}
import breeze.linalg.{*, DenseMatrix}
import breeze.numerics.sigmoid

object PerLayer {

  def graph(input: Source[DenseMatrix[Double], Unit]) = {

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
        val feedForwardFlow = builder.add(Flow[(DenseMatrix[Double], DenseMatrix[Double])].map(x => feedForward(x._1, x._2)))

        zipInputAndWeights.out ~> feedForwardFlow

        new FanInShape2(zipInputAndWeights.in0, zipInputAndWeights.in1, feedForwardFlow.outlet)
      }
    }


    //TODO: Use weights. Now passed in just for readability. They are 0.3 every time
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

          if (layer < topology.length - 1) buildLayer(layer + 1, currentLayer.out, topology, weights) else currentLayer.out
        }

        val flow = builder.add(Flow[DenseMatrix[Double]])
        val network = buildLayer(1, flow.outlet, topology, weights)

        new FlowShape(flow.inlet, network)
      }
    }

    val zipWithIndex =
      FlowGraph.partial() { implicit builder: FlowGraph.Builder[Unit] =>
        import akka.stream.scaladsl.FlowGraph.Implicits._

        val zipWithIndex = builder.add(Zip[DenseMatrix[Double], Int]())
        val index = Source(() => Iterator.from(0, 1))

        index ~> zipWithIndex.in1
                 zipWithIndex.out

        new FlowShape(zipWithIndex.in0, zipWithIndex.out)
      }

    def formatPrintSink() = {
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
      Sink.foreach((x: (DenseMatrix[Double], Int)) =>
        println(s"Output ${x._2} with result ${x._1}in ${format.format(new Date(System.currentTimeMillis()))}"))
    }

    val topology = Array(3, 2, 1)
    val weights = new DenseMatrix[Double](3, 6, Array.fill(3 * 6)(0.3))

    FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import akka.stream.scaladsl.FlowGraph.Implicits._
      input ~> network(topology, weights) ~> zipWithIndex ~> formatPrintSink
    }
  }
}
