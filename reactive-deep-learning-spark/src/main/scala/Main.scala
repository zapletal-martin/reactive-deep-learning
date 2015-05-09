import java.text.SimpleDateFormat
import java.util.Date

import breeze.linalg.{DenseMatrix, *}
import com.github.fommil.netlib.BLAS
import org.apache.spark.SparkContext
import breeze.numerics.sigmoid

object Main extends App {

  def hiddenLayerWeights(topology: Array[Int], layer: Int, weights: Array[DenseMatrix[Double]]) =
    new DenseMatrix[Double](topology(layer), topology(layer - 1), Array.fill(topology(layer) * topology(layer - 1))(0.3))

  def forwardRun(
      topology: Array[Int],
      data: DenseMatrix[Double],
      weightMatrices: Array[DenseMatrix[Double]]): DenseMatrix[Double] = {

    val bias = 0.2
    val outArray = new Array[DenseMatrix[Double]](topology.size)
    val blas = BLAS.getInstance()

    outArray(0) = data

    for(i <- 1 until topology.size) {
      val weights = hiddenLayerWeights(topology, i, weightMatrices)

      val outputCurrent = new DenseMatrix[Double](weights.rows, data.cols)
      val outputPrevious = outArray(i - 1)

      blas.dgemm("N", "N", outputCurrent.rows, outputCurrent.cols,
        weights.cols, 1.0, weights.data, weights.offset, weights.majorStride,
        outputPrevious.data, outputPrevious.offset, outputPrevious.majorStride,
        1.0, outputCurrent.data, outputCurrent.offset, outputCurrent.rows)

      outArray(i) = outputCurrent
      outArray(i)(::, *) :+= bias
      sigmoid.inPlace(outArray(i))
    }

    outArray(topology.size - 1)
  }

  override def main (args: Array[String]) {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    val topology = Array(3, 2, 1)
    val weights = Array(new DenseMatrix[Double](3, 6, Array.fill(3 * 6)(0.3)))

    val sc = new SparkContext("local", "Neural Network")
    sc
      .textFile("src/main/resources/data.csv", 4)
      .map{ l =>
        val splits = l.split(",")
        val features = splits.map(_.toDouble)

        new DenseMatrix(3, 1, Array(features(0), features(1), features(2)))
      }
      .map(in => forwardRun(topology, in, weights))
      .zipWithIndex()
      .foreach(r => println(s"Output ${r._2} with result ${r._1}in ${format.format(new Date(System.currentTimeMillis()))}"))
  }
}
