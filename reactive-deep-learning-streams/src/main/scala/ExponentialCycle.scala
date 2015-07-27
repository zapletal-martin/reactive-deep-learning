import akka.actor.ActorSystem
import akka.stream.{ActorFlowMaterializer, FlowShape}
import akka.stream.scaladsl._

object ExponentialCycle extends App {

  val zipWithIndex =
    FlowGraph.partial() { implicit builder: FlowGraph.Builder[Unit] =>
      import akka.stream.scaladsl.FlowGraph.Implicits._

      val zipWithIndex = builder.add(Zip[Double, Int]())
      val index = Source(() => Iterator.from(1, 1))

      index ~> zipWithIndex.in1
      zipWithIndex.out

      new FlowShape(zipWithIndex.in0, zipWithIndex.out)
    }

  val g = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
    import akka.stream.scaladsl.FlowGraph.Implicits._

    val concat = builder.add(Concat[Double]())
    val input = Source.single(1d)
    val weights = Source.repeat(2d)
    val zip = builder.add(Zip[Double, Double]())

    val transform = builder.add(Flow[(Double, Double)].map{ x =>
      val (input, weights) = x
      val output = input * weights

      output
    })

    val broadcast = builder.add(Broadcast[Double](2))
    val sink = builder.add(Sink.foreach[(Double, Int)](x => println(f"Two to the power of ${x._2} is ${x._1}%1.0f")))

    weights ~> zip.in0
               zip.out ~> transform ~> broadcast
                                       broadcast ~> zipWithIndex ~> sink
               zip.in1 <~ concat    <~ input
                          concat    <~ broadcast
  }

  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorFlowMaterializer()

  g.run()
}
