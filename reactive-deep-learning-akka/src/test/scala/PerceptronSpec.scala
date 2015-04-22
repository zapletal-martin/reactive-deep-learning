import akka.actor.{Props, ActorSystem}
import org.scalatest.FlatSpec

class PerceptronSpec extends FlatSpec {

  "Basic test" should "run" in {
    val system = ActorSystem("test")

    val inputLayer = system.actorOf(Props[InputNode])
    val hiddenLayer = system.actorOf(Props[Perceptron])
    val outputLayer = system.actorOf(Props[OutputNode])

    inputLayer ! AddOutput(Seq(hiddenLayer))

    hiddenLayer ! AddInput(Seq(inputLayer))
    hiddenLayer ! AddOutput(Seq(outputLayer))

    outputLayer ! AddInput(Seq(hiddenLayer))

    Thread.sleep(1000)

    inputLayer ! Input(Array(1.0, 2.0))
    inputLayer ! Input(Array(2.0, 3.0))
    inputLayer ! Input(Array(3.0, 4.0))
  }
}
