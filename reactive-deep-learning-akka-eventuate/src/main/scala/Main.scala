import Node.{UpdateWeightCommand, InputCommand, AddInputsCommand, AddOutputsCommand}
import Edge.{AddInputCommand, AddOutputCommand}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

object Main extends App {

  //**************************************
  // I-----
  //  \    |
  //   ----H
  //  / \ / \
  // I   -   O
  //  \ / \ /
  //   ----H
  //  /    |
  // I-----
  //**************************************
  override def main(params: Array[String]) = {
    val system = ActorSystem("akka")
    val eventLog = system.actorOf(LeveldbEventLog.props(id = "L1", prefix = "log"))

    val parallelModels = Array(
      network(system, eventLog, 0),
      network(system, eventLog, 1),
      network(system, eventLog, 2)
    )

    val file = scala.io.Source.fromFile("src/main/resources/data.csv").getLines().toSeq

    val parallelData = file
      .grouped((file.size.toDouble / parallelModels.size.toDouble).toInt)
      .toSeq

    for(i <- 0 to parallelModels.size - 1) {
      Future {
        parallelData(i)
          .foreach{ l =>
            val splits = l.split(",")

            parallelModels(i)._1 ! InputCommand(splits(0).toDouble)
            parallelModels(i)._2 ! InputCommand(splits(1).toDouble)
            parallelModels(i)._3 ! InputCommand(splits(2).toDouble)
          }
      }
    }

    parallelModels(1)._4 ! UpdateWeightCommand(100)
  }

  private def network(system: ActorSystem, eventLog: ActorRef, replica: Int) = {
    implicit val t = Timeout(10 seconds)
    val d = t.duration

    //Input layer nodes.
    val inputLayer1 = system.actorOf(InputNode.props)
    val inputLayer2 = system.actorOf(InputNode.props)
    val inputLayer3 = system.actorOf(InputNode.props)

    //Hidden layer nodes.
    val hiddenLayer1 = system.actorOf(Perceptron.props(Some("h1"), replica.toString, eventLog))
    val hiddenLayer2 = system.actorOf(Perceptron.props(Some("h2"), replica.toString, eventLog))

    //Output layer nodes.
    val outputLayer = system.actorOf(Props[OutputNode])

    val edgei1h1 = system.actorOf(Edge.props(Some("e1"), replica.toString, eventLog))
    val edgei1h2 = system.actorOf(Edge.props(Some("e2"), replica.toString, eventLog))
    val edgei2h1 = system.actorOf(Edge.props(Some("e3"), replica.toString, eventLog))
    val edgei2h2 = system.actorOf(Edge.props(Some("e4"), replica.toString, eventLog))
    val edgei3h1 = system.actorOf(Edge.props(Some("e5"), replica.toString, eventLog))
    val edgei3h2 = system.actorOf(Edge.props(Some("e6"), replica.toString, eventLog))

    val edgeh1o1 = system.actorOf(Edge.props(Some("e7"), replica.toString, eventLog))
    val edgeh2o1 = system.actorOf(Edge.props(Some("e8"), replica.toString, eventLog))

    //Input layer to hidden layer edges.
    Await.result(edgei1h1 ? AddInputCommand(inputLayer1), d)
    Await.result(edgei1h1 ? AddOutputCommand(hiddenLayer1), d)

    Await.result(edgei1h2 ? AddInputCommand(inputLayer1), d)
    Await.result(edgei1h2 ? AddOutputCommand(hiddenLayer2), d)

    Await.result(edgei2h1 ? AddInputCommand(inputLayer2), d)
    Await.result(edgei2h1 ? AddOutputCommand(hiddenLayer1), d)

    Await.result(edgei2h2 ? AddInputCommand(inputLayer2), d)
    Await.result(edgei2h2 ? AddOutputCommand(hiddenLayer2), d)

    Await.result(edgei3h1 ? AddInputCommand(inputLayer3), d)
    Await.result(edgei3h1 ? AddOutputCommand(hiddenLayer1), d)

    Await.result(edgei3h2 ? AddInputCommand(inputLayer3), d)
    Await.result(edgei3h2 ? AddOutputCommand(hiddenLayer2), d)

    //Hidden layer to output layer edges.
    Await.result(edgeh1o1 ? AddInputCommand(hiddenLayer1), d)
    Await.result(edgeh1o1 ? AddOutputCommand(outputLayer), d)

    Await.result(edgeh2o1 ? AddInputCommand(hiddenLayer2), d)
    Await.result(edgeh2o1 ? AddOutputCommand(outputLayer), d)

    //Linking edges to nodes.
    Await.result(inputLayer1 ? AddOutputsCommand(Seq(edgei1h1, edgei1h2)), d)
    Await.result(inputLayer2 ? AddOutputsCommand(Seq(edgei2h1, edgei2h2)), d)
    Await.result(inputLayer3 ? AddOutputsCommand(Seq(edgei3h1, edgei1h2)), d)

    Await.result(hiddenLayer1 ? AddInputsCommand(Seq(edgei1h1, edgei2h1, edgei3h1)), d)
    Await.result(hiddenLayer1 ? AddOutputsCommand(Seq(edgeh1o1)), d)

    Await.result(hiddenLayer2 ? AddInputsCommand(Seq(edgei1h2, edgei2h2, edgei3h2)), d)
    Await.result(hiddenLayer2 ? AddOutputsCommand(Seq(edgeh2o1)), d)

    Await.result(outputLayer ? AddInputsCommand(Seq(edgeh1o1, edgeh2o1)), d)

    (inputLayer1, inputLayer2, inputLayer3, edgei1h1)
  }
}
