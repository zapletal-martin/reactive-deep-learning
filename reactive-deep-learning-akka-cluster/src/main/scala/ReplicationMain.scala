package replication

import akka.actor._
import akka.cluster.Cluster
import akka.pattern.ask
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import replication.Edge.{UpdateWeight, AddInput, AddOutput}
import replication.Node.{AddInputs, AddOutputs, Input}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object ReplicationMain extends App {

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
  override def main(args: Array[String]) {
    val inputs = startup(Seq("2551", "2552", "0"))

    val file = scala.io.Source.fromFile("src/main/resources/data.csv").getLines().toSeq

    val parallelData = file
      .grouped((file.size.toDouble / inputs.size.toDouble).toInt)
      .toSeq

    import scala.concurrent.ExecutionContext.Implicits.global

    for(i <- 0 to inputs.size - 1) {
      Future {
        parallelData(i)
          .foreach{ l =>
          val splits = l.split(",")

          inputs(i)._1 ! Input(splits(0).toDouble)
          inputs(i)._2 ! Input(splits(1).toDouble)
          inputs(i)._3 ! Input(splits(2).toDouble)
        }
      }
    }

    (0 to 2)
      .par
      .foreach{ j =>
        inputs(0)._4 ! UpdateWeight(0 + j)
        inputs(1)._4 ! UpdateWeight(3 + j)
        inputs(2)._4 ! UpdateWeight(6 + j)
    }
  }

  def startup(ports: Seq[String]) = {
    ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory
        .parseString("akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      val cluster = Cluster(system)
      //cluster.joinSeedNodes(Seq(Address()))

      startupSharedJournal(system, startStore = port == "2551", path =
        ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"))

      //Input layer nodes.
      val inputLayer1 = system.actorOf(InputNode.props(), "inputLayer1")
      val inputLayer2 = system.actorOf(InputNode.props(), "inputLayer2")
      val inputLayer3 = system.actorOf(InputNode.props(), "inputLayer3")

      //Hidden layer nodes.
      val hiddenLayer1 = system.actorOf(Perceptron.props(), "hiddenLayer1")
      val hiddenLayer2 = system.actorOf(Perceptron.props(), "hiddenLayer2")

      //Output layer nodes.
      val outputLayer = system.actorOf(Perceptron.props(), "outputLayer")

      //Printer.
      val printer = system.actorOf(OutputNode.props(), "printer")

      //Edges.
      val edgei1h1 = system.actorOf(Edge.props(), s"edgei1h1r$port")
      val edgei1h2 = system.actorOf(Edge.props(), "edgei1h2")
      val edgei2h1 = system.actorOf(Edge.props(), "edgei2h1")
      val edgei2h2 = system.actorOf(Edge.props(), "edgei2h2")
      val edgei3h1 = system.actorOf(Edge.props(), "edgei3h1")
      val edgei3h2 = system.actorOf(Edge.props(), "edgei3h2")

      val edgeh1o1 = system.actorOf(Edge.props(), "edgeh1o1")
      val edgeh2o1 = system.actorOf(Edge.props(), "edgeh2o1")

      val edgeo1p1 = system.actorOf(Edge.props(), "edgeo1p1")

      implicit val t = Timeout(10 seconds)
      val d = t.duration

      Await.result(edgei1h1 ? AddInput(inputLayer1), d)
      Await.result(edgei1h1 ? AddOutput(hiddenLayer1), d)

      Await.result(edgei1h2 ? AddInput(inputLayer1), d)
      Await.result(edgei1h2 ? AddOutput(hiddenLayer2), d)

      Await.result(edgei2h1 ? AddInput(inputLayer2), d)
      Await.result(edgei2h1 ? AddOutput(hiddenLayer1), d)

      Await.result(edgei2h2 ? AddInput(inputLayer2), d)
      Await.result(edgei2h2 ? AddOutput(hiddenLayer2), d)

      Await.result(edgei3h1 ? AddInput(inputLayer3), d)
      Await.result(edgei3h1 ? AddOutput(hiddenLayer1), d)

      Await.result(edgei3h2 ? AddInput(inputLayer3), d)
      Await.result(edgei3h2 ? AddOutput(hiddenLayer2), d)

      //Hidden layer to output layer edges.
      Await.result(edgeh1o1 ? AddInput(hiddenLayer1), d)
      Await.result(edgeh1o1 ? AddOutput(outputLayer), d)

      Await.result(edgeh2o1 ? AddInput(hiddenLayer2), d)
      Await.result(edgeh2o1 ? AddOutput(outputLayer), d)

      //Output layer to printer.
      Await.result(edgeo1p1 ? AddInput(outputLayer), d)
      Await.result(edgeo1p1 ? AddOutput(printer), d)

      //Linking edges to nodes.
      Await.result(inputLayer1 ? AddOutputs(Seq(edgei1h1, edgei1h2)), d)
      Await.result(inputLayer2 ? AddOutputs(Seq(edgei2h1, edgei2h2)), d)
      Await.result(inputLayer3 ? AddOutputs(Seq(edgei3h1, edgei3h2)), d)

      Await.result(hiddenLayer1 ? AddInputs(Seq(edgei1h1, edgei2h1, edgei3h1)), d)
      Await.result(hiddenLayer1 ? AddOutputs(Seq(edgeh1o1)), d)

      Await.result(hiddenLayer2 ? AddInputs(Seq(edgei1h2, edgei2h2, edgei3h2)), d)
      Await.result(hiddenLayer2 ? AddOutputs(Seq(edgeh2o1)), d)

      Await.result(outputLayer ? AddInputs(Seq(edgeh1o1, edgeh2o1)), d)
      Await.result(outputLayer ? AddOutputs(Seq(edgeo1p1)), d)

      Await.result(printer ? AddInputs(Seq(edgeo1p1)), d)

      (inputLayer1, inputLayer2, inputLayer3, edgei1h1)
    }
  }

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val timeout = Timeout(15.seconds)
    val f = (system.actorSelection(path) ? Identify(None))
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.shutdown()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.shutdown()
    }
  }
}