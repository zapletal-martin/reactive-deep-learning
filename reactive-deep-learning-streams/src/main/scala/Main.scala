import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._

object Main extends App {
  override def main (args: Array[String]) {

    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorFlowMaterializer()

    val sourceFile = scala.io.Source.fromFile("src/main/resources/data.csv")

    val input = Source(() => sourceFile.getLines())
      .map{ l =>
        val splits = l.split(",")
        val features = splits.map(_.toDouble)

        Seq(features(0), features(1), features(2))
      }

      //PerNode.graph(input).run()
      PerNetwork.graph(input).run();
  }
}
