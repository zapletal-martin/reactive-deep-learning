import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source

object Main extends App {
  override def main (args: Array[String]) {

    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorFlowMaterializer()

    val sourceFile = scala.io.Source.fromFile("src/main/resources/data.csv")

    Source(() => sourceFile.getLines())
      .map{ l =>
        val splits = l.split(",")
        splits.map(_.toDouble)
    }
    .runForeach(x => println(s"${x(0)}, ${x(1)}, ${x(2)}"))
  }
}
