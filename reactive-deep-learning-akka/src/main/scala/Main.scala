import akka.actor.{Props, ActorSystem}

object Main extends App {

  override def main(params: Array[String]) = {
    val system = ActorSystem("test")

    val inputLayer = system.actorOf(Props[InputNode])
    val hiddenLayer = system.actorOf(Props[Perceptron])
    val outputLayer = system.actorOf(Props[OutputNode])

    //**************************************
    // o -> h -> i
    //**************************************
    inputLayer ! AddOutput(Seq(hiddenLayer))

    hiddenLayer ! AddInput(Seq(inputLayer))
    hiddenLayer ! AddOutput(Seq(outputLayer))

    outputLayer ! AddInput(Seq(hiddenLayer))

    /*inputLayer ! Input(Array(1.0, 2.0))
    inputLayer ! Input(Array(2.0, 3.0))
    inputLayer ! Input(Array(3.0, 4.0))*/

    while(true) {
      val in = scala.io.StdIn.readLine()
      val ind = in.toDouble
      inputLayer ! Input(Array(ind))
    }
  }

}
