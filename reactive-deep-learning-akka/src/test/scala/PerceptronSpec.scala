import Node.{WeightedInput, AddInputs, AddOutputs}
import akka.actor.{Props, ActorSystem}
import org.scalatest.FlatSpec

class PerceptronSpec extends FlatSpec {

  "Basic test" should "run" in {
    val system = ActorSystem("test")

    val inputLayer = system.actorOf(Props[InputNode])
    val hiddenLayer = system.actorOf(Props[Perceptron])
    val outputLayer = system.actorOf(Props[OutputNode])

    inputLayer ! AddOutputs(Seq(hiddenLayer))

    hiddenLayer ! AddInputs(Seq(inputLayer))
    hiddenLayer ! AddOutputs(Seq(outputLayer))

    outputLayer ! AddInputs(Seq(hiddenLayer))

    Thread.sleep(1000)

    inputLayer ! WeightedInput(1.0, 2.0)
    inputLayer ! WeightedInput(2.0, 3.0)
    inputLayer ! WeightedInput(3.0, 4.0)
  }
}
