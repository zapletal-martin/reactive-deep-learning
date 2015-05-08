import java.text.SimpleDateFormat
import java.util.Date

import akka.stream.scaladsl._
import breeze.linalg.{*, DenseMatrix}
import breeze.numerics.sigmoid

object PerLayer {

  def graph(input: Source[Seq[Double], Unit]) = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    val hiddenLayerWeightsVector = Array.fill(6)(.3)
    val outputLayerWeightsVector = Array.fill(2)(.3)
    val bias = 0.2

    val hiddenLayerWeights = Source(() => Iterator.continually(hiddenLayerWeightsVector))
    val outputLayerWeights = Source(() => Iterator.continually(outputLayerWeightsVector))

    val topology = Array(3, 2, 1)

    def weights(w: Array[Double], layer: Int) = new DenseMatrix[Double](topology(layer), topology(layer - 1), w)
    def toMatrix(i: Seq[Double]) = new DenseMatrix(topology(0), 1, i.toArray)

    def feedForward(data: DenseMatrix[Double], weightMatrices: DenseMatrix[Double]) = {
      val activation = weightMatrices * data
      activation(::, *) :+= bias
      sigmoid.inPlace(activation)
      activation
    }

    val g = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      val weightsFlow = Flow[Array[Double]].map(weights(_, 1))
      val weightsFlow2 = Flow[Array[Double]].map(weights(_, 2))
      val inputsFlow = Flow[Seq[Double]].map(toMatrix)
      val zipInputAndWeights = builder.add(Zip[DenseMatrix[Double], DenseMatrix[Double]]())
      val zipInputAndWeights2 = builder.add(Zip[DenseMatrix[Double], DenseMatrix[Double]]())
      val feedForwardFlow = Flow[(DenseMatrix[Double], DenseMatrix[Double])].map(x => feedForward(x._1, x._2))
      val zipWithIndex = builder.add(Zip[DenseMatrix[Double], Int]())
      val index = Source(() => Iterator.from(0, 1))
      val out = Sink
        .foreach((x: (DenseMatrix[Double], Int)) => println(s"Output ${x._2} with result ${x._1}in ${format.format(new Date(System.currentTimeMillis()))}"))

      input              ~> inputsFlow  ~> zipInputAndWeights.in0
      hiddenLayerWeights ~> weightsFlow ~> zipInputAndWeights.in1
                                           zipInputAndWeights.out ~> feedForwardFlow ~> zipInputAndWeights2.in0
                                           outputLayerWeights     ~> weightsFlow2    ~> zipInputAndWeights2.in1
                                                                                        zipInputAndWeights2.out ~> feedForwardFlow ~> zipWithIndex.in0
                                                                                                                             index ~> zipWithIndex.in1
                                                                                                                                      zipWithIndex.out ~> out
    }

    g
  }
}
