import Node.{Input, AddInputs, AddOutputs}
import Edge.{AddOutput, AddInput}
import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.pattern.ask

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
          Perceptron.shardName, Some(Props[Perceptron]), None, false, Node.idExtractor, Node.shardResolver)

        val inputNodes = ClusterSharding(system).start(
          InputNode.shardName, Some(Props[InputNode]), None, false, Node.idExtractor, Node.shardResolver)

        val outputNodes = ClusterSharding(system).start(
          OutputNode.shardName, Some(Props[OutputNode]), None, false, Node.idExtractor, Node.shardResolver)

        val edges = ClusterSharding(system).start(
          Edge.shardName, Some(Props[Edge]), None, false, Edge.idExtractor, Edge.shardResolver)

        if(port == "0") {
          //Input layer to hidden layer edges.
          edges ! AddInput("e-1-1-2-1", "n-1-1")
          edges ! AddOutput("e-1-1-2-1", "n-2-1")

          edges ! AddInput("e-1-2-2-1", "n-1-2")
          edges ! AddOutput("e-1-2-2-1", "n-2-1")

          edges ! AddInput("e-1-3-2-1", "n-1-3")
          edges ! AddOutput("e-1-1-2-3", "n-2-1")

          edges ! AddInput("e-1-1-2-2", "n-1-1")
          edges ! AddOutput("e-1-1-2-2", "n-2-2")

          edges ! AddInput("e-1-2-2-2", "n-1-2")
          edges ! AddOutput("e-1-2-2-2", "n-2-2")

          edges ! AddInput("e-1-3-2-2", "n-1-3")
          edges ! AddOutput("e-1-3-2-2", "n-2-2")

          //Hidden layer to output layer edges.
          edges ! AddInput("e-2-1-3-1", "n-2-1")
          edges ! AddOutput("e-2-1-3-1", "n-3-1")

          edges ! AddInput("e-2-2-3-1", "n-2-2")
          edges ! AddOutput("e-2-2-3-1", "n-3-1")

          //Linking edges to nodes.
          inputNodes ! AddOutputs("n-1-1", Seq("e-1-1-2-1", "e-1-1-2-2"))
          inputNodes ! AddOutputs("n-1-2", Seq("e-1-2-2-1", "e-1-2-2-2"))
          inputNodes ! AddOutputs("n-1-3", Seq("e-1-3-2-1", "e-1-3-2-2"))

          nodes ! AddInputs("n-2-1", Seq("e-1-1-2-1", "e-1-2-2-1", "e-1-3-2-1"))
          nodes ! AddOutputs("n-2-1", Seq("e-2-1-3-1"))

          nodes ! AddInputs("n-2-2", Seq("e-1-1-2-2", "e-1-2-2-2", "e-1-3-2-2"))
          nodes ! AddOutputs("n-2-2", Seq("e-2-2-3-1"))

          outputNodes ! AddInputs("n-3-1", Seq("e-2-1-3-1", "e-2-2-3-1"))

          scala.io.Source.fromFile("src/main/resources/data.csv")
            .getLines()
            .foreach { l =>
            val splits = l.split(",")

            inputNodes ! Input("n-1-1", splits(0).toDouble)
            inputNodes ! Input("n-1-2", splits(1).toDouble)
            inputNodes ! Input("n-1-3", splits(2).toDouble)
          }

          val reaper = system.actorOf(Props(new Actor {
            override def receive: Receive = {
              case _ => system.terminate()
            }
          }))

          import system.dispatcher
          system.scheduler.scheduleOnce(100 seconds, reaper, 'bye)
        }
    }
  }

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")
    // register the shared journal
    import system.dispatcher
    implicit val timeout = Timeout(15.seconds)
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
