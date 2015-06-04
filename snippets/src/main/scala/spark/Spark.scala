package spark

import java.text.SimpleDateFormat

import breeze.linalg.{*, DenseMatrix}
import breeze.numerics.sigmoid
import com.github.fommil.netlib.BLAS
import org.apache.spark.{AccumulatorParam, SparkContext}

object Spark {
  object DoubleAccumulatorParam extends AccumulatorParam[Double] {
    def zero(initialValue: Double): Double = 0
    def addInPlace(d1: Double, d2: Double): Double = d1 + d2
  }

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

  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  val topology = Array(3, 2, 1)
  val weights = Array(new DenseMatrix[Double](3, 6, Array.fill(3 * 6)(0.3)))

  val sc = new SparkContext("local", "Neural Network")

  /*sc.textFile("counts")
    .map(line => line.split("\t"))
    .map(w => (w(0), w(1).toInt))
    .reduceByKey(_ + _)
    .collect()*/

  val result = sc
    .textFile("src/main/resources/data.csv", 3)
    .map{ l =>
      val splits = l.split(",")
      val features = splits.map(_.toDouble)

      new DenseMatrix(3, 1, Array(features(0), features(1), features(2)))
    }
    .map(in => forwardRun(topology, in, weights).toString)

  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  import sqlContext.implicits._

  val resultDF = result.toDF("result")
  resultDF
    .filter(resultDF("result") > "String")
    .select(resultDF("result"))

  // StructType(StructField(result,StringType,true))

  resultDF.registerTempTable("results")
  val filtered3 = sqlContext.sql("SELECT result FROM (SELECT result FROM results) r WHERE r.result >= \"String\"")
}

object Catalyst {
  case class Person(name: String, age: Int)

  val sc = new SparkContext("local[4]", "Catalyst")
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  import spark.Catalyst.sqlContext.implicits._

  val people = sc.parallelize(
    List(
      Person("John", 25),
      Person("Mark", 30),
      Person("Josh", 35),
      Person("Harry", 40),
      Person("Fred", 45)))

  val filtered = people
    .filter(p => p.age >= 20 && p.age <= 35)
    .map(_.name)
    .filter(_ == "John")

  val peopleDF = people.toDF()
  val filtered2 = peopleDF
    .select(peopleDF("name"), peopleDF("age"))
    .where(peopleDF("age") >= 20 && peopleDF("age") <= 35)
    .where(peopleDF("name") === "John")

  // StructType(StructField(name,StringType,true), StructField(age,IntegerType,false))

  val typeCheck = peopleDF
    .where(peopleDF("age") === "John")
    .select(peopleDF("name") * 2)

  peopleDF.registerTempTable("people")
  val filtered3 = sqlContext.sql("" +
    "SELECT name " +
    "FROM (" +
    "  SELECT age, name " +
    "  FROM people) p " +
    "WHERE p.age >= 20 AND p.age <= 35 AND p.name = \"John\"")

}

object FilterPushdown {
  val sc = new SparkContext("local[4]", "Catalyst")

  case class Person(age: Int, height: Double)
  val people = sc.parallelize((0 to 100).map(x => Person(x, x)))

  people
    .map(p => Person(p.age, p.height * 2.54))
    .filter(_.age < 35)

  people
    .filter(_.age < 35)
    .map(p => Person(p.age, p.height * 2.54))

  people
    .map(p => Person(p.age, p.height * 2.54))
    .filter(_.height < 170)

  people
    .filter(_.height < 170)
    .map(p => Person(p.age, p.height * 2.54))



}