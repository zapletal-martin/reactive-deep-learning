import akka.stream.scaladsl._

//TODO: I don't think this is correct. Apply H->O weights
object PerNode {
  def graph(input: Source[Seq[Double], Unit]) = {
    val hiddenLayerWeights = Array.fill(6)(.2).toSeq
    val hiddenLayer = Source(() => Iterator.continually(hiddenLayerWeights))

    val g = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      val weightSplit = Flow[Seq[Double]]
        .map { x =>
          val firstWeights = x.zipWithIndex.filter(_._2 % 2 == 0).map(_._1)
          val secondWeights = x.zipWithIndex.filter(_._2 % 2 != 0).map(_._1)

          (firstWeights, secondWeights)
        }

      val unzip = builder.add(Unzip[Seq[Double], Seq[Double]]())
      val zip = builder.add(Zip[Seq[Double], Seq[Double]]())
      val zip2 = builder.add(Zip[Seq[Double], Seq[Double]]())

      val perceptron = Flow[(Seq[Double], Seq[Double])]
        .map(i => Neuron.sigmoid(i._1.zip(i._2).map(x => x._1 * x._2).sum + 0.2))

      val outputLayerMerge = builder.add(Merge[Double](2))
      val zipWithIndex = builder.add(Zip[Double, Int]())
      val index = Source(() => Iterator.from(0, 1))
      val out = Sink.foreach(println)
      val inputVectorSplit = builder.add(Broadcast[Seq[Double]](2))

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

    g
  }
}
