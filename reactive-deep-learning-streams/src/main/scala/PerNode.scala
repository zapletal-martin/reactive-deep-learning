import akka.stream.scaladsl._

//TODO: I don't think this is correct. Apply H->O weights
object PerNode {
  def graph(input: Source[Array[Double], Unit]) = {
    val hiddenLayerWeights = Array.fill(6)(.3)
    val hiddenLayer = Source(() => Iterator.continually(hiddenLayerWeights))

    FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      val weightSplit = Flow[Array[Double]]
        .map { x =>
          val firstWeights = x.zipWithIndex.filter(_._2 % 2 == 0).map(_._1)
          val secondWeights = x.zipWithIndex.filter(_._2 % 2 != 0).map(_._1)

          (firstWeights, secondWeights)
        }

      val unzip = builder.add(Unzip[Array[Double], Array[Double]]())
      val zip = builder.add(Zip[Array[Double], Array[Double]]())
      val zip2 = builder.add(Zip[Array[Double], Array[Double]]())

      val perceptron = Flow[(Array[Double], Array[Double])]
        .map(i => Neuron.sigmoid(i._1.zip(i._2).map(x => x._1 * x._2).sum + 0.2))

      val outputLayerMerge = builder.add(Merge[Double](2))
      val zipWithIndex = builder.add(Zip[Double, Int]())
      val index = Source(() => Iterator.from(0, 1))
      val out = Sink.foreach(println)
      val inputVectorSplit = builder.add(Broadcast[Array[Double]](2))

      input ~> inputVectorSplit.in
               inputVectorSplit.out(0) ~> zip.in0
               inputVectorSplit.out(1) ~> zip2.in0

      hiddenLayer ~> weightSplit ~> unzip.in
                                    unzip.out0 ~> zip.in1
                                    unzip.out1 ~> zip2.in1
                                                  zip.out ~> perceptron ~> outputLayerMerge.in(0)
                                                  zip2.out ~> perceptron ~> outputLayerMerge.in(1)
                                                                            outputLayerMerge.out ~> zipWithIndex.in0
                                                                                           index ~> zipWithIndex.in1
                                                                                                    zipWithIndex.out ~> out
    }
  }
}
