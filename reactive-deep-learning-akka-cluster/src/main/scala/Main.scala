import Node.{Input, AddInput, AddOutput}
import akka.actor.{Actor, ActorSystem, Props}
import akka.contrib.pattern.ClusterSharding
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object Main extends App {

  override def main(args: Array[String]): Unit = {
    startup(Seq("2551", "2552", "0"))
  }

  def startup(ports: Seq[String]) = {
    ports foreach { port =>
      if (port != "0") {
        // Override the configuration of the port
        val config = ConfigFactory
          .parseString("akka.remote.netty.tcp.port=" + port)
          .withFallback(ConfigFactory.load())

        // Create an Akka system
        val system = ActorSystem("ClusterSystem", config)

        ClusterSharding(system).start(
          Perceptron.shardName, Some(Props[Perceptron]), None, false, Perceptron.idExtractor, Perceptron.shardResolver
        )

        //Input layer nodes.
        /*val inputLayer1 = system.actorOf(Props[InputNode])
        val inputLayer2 = system.actorOf(Props[InputNode])
        val inputLayer3 = system.actorOf(Props[InputNode])

        //Hidden layer nodes.
        val hiddenLayer1 = system.actorOf(Props[Perceptron])
        val hiddenLayer2 = system.actorOf(Props[Perceptron])

        //Output layer nodes.
        val outputLayer = system.actorOf(Props[OutputNode])

        //Input layer to hidden layer edges.
        val edgei1h1 = system.actorOf(Props(new Edge("n-1-1", "n-2-1")))
        val edgei1h2 = system.actorOf(Props(new Edge("n-1-1", "n-2-2")))
        val edgei2h1 = system.actorOf(Props(new Edge("n-1-2", "n-2-1")))
        val edgei2h2 = system.actorOf(Props(new Edge("n-1-2", "n-2-2")))
        val edgei3h1 = system.actorOf(Props(new Edge("n-1-3", "n-2-1")))
        val edgei3h2 = system.actorOf(Props(new Edge("n-1-3", "n-2-2")))*/

        //Hidden layer to output layer edges.
        val edgeh1o1 = system.actorOf(Props(new Edge("n-2-1", "n-3-1")))
        val edgeh2o1 = system.actorOf(Props(new Edge("n-2-2", "n-3-1")))


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

      } else {

        val config = ConfigFactory
          .parseString("akka.remote.netty.tcp.port=" + port)
          .withFallback(ConfigFactory.load())

        // Create an Akka system
        val system = ActorSystem("ClusterSystem", config)

        val nodes = ClusterSharding(system).start(
          Perceptron.shardName, Some(Props[Perceptron]), None, false, Perceptron.idExtractor, Perceptron.shardResolver
        )

        nodes ! AddOutput("n-1-1", Seq("e-1-1-2-1", "e-1-1-2-2"))
        nodes ! AddOutput("n-1-2", Seq("e-1-2-2-1", "e-1-2-2-2"))
        nodes ! AddOutput("n-1-3", Seq("e-1-3-2-1", "e-1-3-2-2"))

        nodes ! AddInput("n-2-1", Seq("e-1-1-2-1", "e-1-2-2-1", "e-1-3-2-1"))
        nodes ! AddOutput("n-2-1", Seq("e-2-1-3-1"))

        nodes ! AddInput("n-2-2", Seq("e-1-1-2-2", "e-1-2-2-2", "e-1-3-2-2"))
        nodes ! AddOutput("n-2-2", Seq("e-2-2-3-1"))

        nodes ! AddInput("n-3-1", Seq("e-2-1-3-1", "e-2-2-3-1"))

        scala.io.Source.fromFile("src/main/resources/data.csv")
          .getLines()
          .foreach { l =>
          Thread.sleep(1000)
          val splits = l.split(",")

          nodes ! Input("n-1-1", splits(0).toDouble)
          nodes ! Input("n-1-2", splits(1).toDouble)
          nodes ! Input("n-1-3", splits(2).toDouble)
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
}
