import java.text.SimpleDateFormat
import java.util.Date

import breeze.linalg.{*, DenseMatrix}
import akka.stream.scaladsl._
import breeze.numerics.sigmoid

object PerNetwork {
  def graph(input: Source[Seq[Double], Unit]) = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    val weightsVector = Array.fill(8)(0.3)
    val bias = 0.2

    val topology = Array(3, 2, 1)

    val weightsSource = Source(() => Iterator.continually(weightsVector))

    def weights(w: Array[Double]) = {
      val weightMatrices = new Array[DenseMatrix[Double]](topology.size)

      var offset = 0
      for(i <- 1 until topology.size){
        weightMatrices(i) = new DenseMatrix[Double](topology(i), topology(i - 1), w, offset)
        offset += topology(i) * topology(i - 1)
      }

      weightMatrices
    }

    def toMatrix(i: Seq[Double]) = {
      new DenseMatrix(topology(0), 1, i.toArray)
    }

    def feedForward(data: DenseMatrix[Double], weightMatrices: Array[DenseMatrix[Double]]) = {
      val outArray = new Array[DenseMatrix[Double]](topology.size)
      outArray(0) = data
      for(i <- 1 until topology.size) {
        outArray(i) = weightMatrices(i) * outArray(i - 1)
        outArray(i)(::, *) :+= bias
        sigmoid.inPlace(outArray(i))
      }

      outArray.last
    }

    val g = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      val weightsFlow = Flow[Array[Double]]
        .map(weights)

      val inputsFlow = Flow[Seq[Double]]
        .map(toMatrix)

      val feedForwardFlow = Flow[(DenseMatrix[Double], Array[DenseMatrix[Double]])]
        .map(x => feedForward(x._1, x._2))

      val zipInputAndWeights = builder.add(Zip[DenseMatrix[Double], Array[DenseMatrix[Double]]]())

      val zipWithIndex = builder.add(Zip[DenseMatrix[Double], Int]())

      val index = Source(() => Iterator.from(0, 1))

      val out = Sink
        .foreach((x: (DenseMatrix[Double], Int)) => println(s"Output ${x._2} with result ${x._1}in ${format.format(new Date(System.currentTimeMillis()))}"))

      input         ~> inputsFlow  ~> zipInputAndWeights.in0
      weightsSource ~> weightsFlow ~> zipInputAndWeights.in1
                                      zipInputAndWeights.out ~> feedForwardFlow ~> zipWithIndex.in0
                                                                          index ~> zipWithIndex.in1
                                                                                   zipWithIndex.out ~> out
    }

    g
  }
}
