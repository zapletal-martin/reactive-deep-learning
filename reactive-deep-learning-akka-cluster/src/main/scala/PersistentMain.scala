package persistence

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.pattern.ask
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import persistence.Edge.{AddOutputCommand, AddInputCommand}
import persistence.Node.{AddInputsCommand, AddOutputsCommand, Input}

import scala.concurrent.Await
import scala.concurrent.duration._

object PersistentMain extends App {

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
  override def main(args: Array[String]): Unit = {
    startup(Seq("2551", "2552", "0"))
  }

  def startup(ports: Seq[String]) = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory
        .parseString("akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)

      startupSharedJournal(system, startStore = port == "2551", path =
        ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"))

      val nodes = ClusterSharding(system).start(
        Perceptron.shardName, Some(Perceptron.props), None, false, Node.idExtractor, Node.shardResolver)

      val inputNodes = ClusterSharding(system).start(
        InputNode.shardName, Some(InputNode.props), None, false, Node.idExtractor, Node.shardResolver)

      val outputNodes = ClusterSharding(system).start(
        OutputNode.shardName, Some(OutputNode.props), None, false, Node.idExtractor, Node.shardResolver)

      val edges = ClusterSharding(system).start(
        Edge.shardName, Some(Edge.props), None, false, Edge.idExtractor, Edge.shardResolver)

      if(port == "0") {
        implicit val t = Timeout(10 seconds)
        val d = t.duration

        //Input layer to hidden layer edges.
        Await.result(edges ? AddInputCommand("e-1-1-2-1", "n-1-1"), d)
        Await.result(edges ? AddOutputCommand("e-1-1-2-1", "n-2-1"), d)

        Await.result(edges ? AddInputCommand("e-1-2-2-1", "n-1-2"), d)
        Await.result(edges ? AddOutputCommand("e-1-2-2-1", "n-2-1"), d)

        Await.result(edges ? AddInputCommand("e-1-3-2-1", "n-1-3"), d)
        Await.result(edges ? AddOutputCommand("e-1-3-2-1", "n-2-1"), d)

        Await.result(edges ? AddInputCommand("e-1-1-2-2", "n-1-1"), d)
        Await.result(edges ? AddOutputCommand("e-1-1-2-2", "n-2-2"), d)

        Await.result(edges ? AddInputCommand("e-1-2-2-2", "n-1-2"), d)
        Await.result(edges ? AddOutputCommand("e-1-2-2-2", "n-2-2"), d)

        Await.result(edges ? AddInputCommand("e-1-3-2-2", "n-1-3"), d)
        Await.result(edges ? AddOutputCommand("e-1-3-2-2", "n-2-2"), d)

        //Hidden layer to output layer edges.
        Await.result(edges ? AddInputCommand("e-2-1-3-1", "n-2-1"), d)
        Await.result(edges ? AddOutputCommand("e-2-1-3-1", "o-3-1"), d)

        Await.result(edges ? AddInputCommand("e-2-2-3-1", "n-2-2"), d)
        Await.result(edges ? AddOutputCommand("e-2-2-3-1", "o-3-1"), d)

        //Output layer to printer edge.
        Await.result(edges ? AddInputCommand("e-3-1-4-1", "o-3-1"), d)
        Await.result(edges ? AddOutputCommand("e-3-1-4-1", "p-4-1"), d)

        //Linking edges to nodes.
        Await.result(inputNodes ? AddOutputsCommand("n-1-1", Seq("e-1-1-2-1", "e-1-1-2-2")), d)
        Await.result(inputNodes ? AddOutputsCommand("n-1-2", Seq("e-1-2-2-1", "e-1-2-2-2")), d)
        Await.result(inputNodes ? AddOutputsCommand("n-1-3", Seq("e-1-3-2-1", "e-1-3-2-2")), d)

        Await.result(nodes ? AddInputsCommand("n-2-1", Seq("e-1-1-2-1", "e-1-2-2-1", "e-1-3-2-1")), d)
        Await.result(nodes ? AddOutputsCommand("n-2-1", Seq("e-2-1-3-1")), d)

        Await.result(nodes ? AddInputsCommand("n-2-2", Seq("e-1-1-2-2", "e-1-2-2-2", "e-1-3-2-2")), d)
        Await.result(nodes ? AddOutputsCommand("n-2-2", Seq("e-2-2-3-1")), d)

        Await.result(nodes ? AddInputsCommand("o-3-1", Seq("e-2-1-3-1", "e-2-2-3-1")), d)
        Await.result(nodes ? AddOutputsCommand("o-3-1", Seq("e-3-1-4-1")), d)

        Await.result(outputNodes ? AddInputsCommand("p-4-1", Seq("e-3-1-4-1")), d)

        scala.io.Source.fromFile("src/main/resources/data.csv")
          .getLines()
          .foreach { l =>
            val splits = l.split(",")

            inputNodes ! Input("n-1-1", splits(0).toDouble)
            inputNodes ! Input("n-1-2", splits(1).toDouble)
            inputNodes ! Input("n-1-3", splits(2).toDouble)
          }
      }
    }
  }

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")

    import system.dispatcher
    implicit val timeout = Timeout(15 seconds)
    val f = (system.actorSelection(path) ? Identify(None))
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.terminate()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.terminate()
    }
  }
}
