import java.text.SimpleDateFormat
import java.util.Date

import akka.stream.FanInShape2
import akka.stream.scaladsl._
import breeze.linalg.{*, DenseMatrix}
import breeze.numerics.sigmoid

object PerLayer {

  def graph(input: Source[DenseMatrix[Double], Unit]) = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    val bias = 0.2
    val topology = Array(3, 2, 1)

    def hiddenLayerWeights(layer: Int) = {
      def hiddenLayerWeightsMatrix(layer: Int) =
        new DenseMatrix[Double](topology(layer), topology(layer - 1), Array.fill(topology(layer) * topology(layer - 1))(0.3))

      Source(() => Iterator.continually(hiddenLayerWeightsMatrix(layer)))
    }

    def hiddenLayer(layer: Int) = {
      def feedForward(data: DenseMatrix[Double], weightMatrices: DenseMatrix[Double]) = {
        val activation: DenseMatrix[Double] = weightMatrices * data
        activation(::, *) :+= bias
        sigmoid.inPlace(activation)
        activation
      }

      FlowGraph.partial() { implicit builder: FlowGraph.Builder[Unit] =>
        import FlowGraph.Implicits._

        val zipInputAndWeights = builder.add(Zip[DenseMatrix[Double], DenseMatrix[Double]]())
        val feedForwardFlow = builder.add(Flow[(DenseMatrix[Double], DenseMatrix[Double])].map(x => feedForward(x._1, x._2)))

        zipInputAndWeights.out ~> feedForwardFlow

        new FanInShape2(zipInputAndWeights.in0, zipInputAndWeights.in1, feedForwardFlow.outlet)
      }
    }

    FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      val zipWithIndex = builder.add(Zip[DenseMatrix[Double], Int]())
      val index = Source(() => Iterator.from(0, 1))
      val out = Sink
        .foreach((x: (DenseMatrix[Double], Int)) => println(s"Output ${x._2} with result ${x._1}in${format.format(new Date(System.currentTimeMillis()))}"))

      val layer1 = builder.add(hiddenLayer(1))
      val layer2 = builder.add(hiddenLayer(2))

      input                 ~> layer1.in0
      hiddenLayerWeights(1) ~> layer1.in1
                               layer1.out            ~> layer2.in0
                               hiddenLayerWeights(2) ~> layer2.in1
                                                        layer2.out ~> zipWithIndex.in0
                                                             index ~> zipWithIndex.in1
                                                                      zipWithIndex.out ~> out
    }
  }
}
