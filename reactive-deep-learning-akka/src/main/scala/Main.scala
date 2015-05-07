import Node.{Input, AddInputs, AddOutputs, Ack}
import Edge.{AddInput, AddOutput}
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.typed._
import akka.typed.ScalaDSL._
import akka.typed.AskPattern._


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

    val main: Behavior[Unit] =
      Full {
        case Sig(ctx, PreStart) =>

          //Input layer nodes.
          val inputLayer1 = ctx.spawn(InputNode.props(), "inputLayer1")
          val inputLayer2 = ctx.spawn(InputNode.props(), "inputLayer2")
          val inputLayer3 = ctx.spawn(InputNode.props(), "inputLayer3")

          //Hidden layer nodes.
          val hiddenLayer1 = ctx.spawn(Perceptron.props(), "hiddenLayer1")
          val hiddenLayer2 = ctx.spawn(Perceptron.props(), "hiddenLayer2")

          //Output layer nodes.
          val outputLayer = ctx.spawn(Perceptron.props(), "outputLayer")

          //Printer.
          val printer = ctx.spawn(OutputNode.props(), "printer")

          //Edges.
          val edgei1h1 = ctx.spawn(Edge.props(), "edgei1h1")
          val edgei1h2 = ctx.spawn(Edge.props(), "edgei1h2")
          val edgei2h1 = ctx.spawn(Edge.props(), "edgei2h1")
          val edgei2h2 = ctx.spawn(Edge.props(), "edgei2h2")
          val edgei3h1 = ctx.spawn(Edge.props(), "edgei3h1")
          val edgei3h2 = ctx.spawn(Edge.props(), "edgei3h2")

          val edgeh1o1 = ctx.spawn(Edge.props(), "edgeh1o1")
          val edgeh2o1 = ctx.spawn(Edge.props(), "edgeh2o1")

          val edgeo1p1 = ctx.spawn(Edge.props(), "edgeo1p1")

          implicit val t = Timeout(10 seconds)
          val d = t.duration

          type ack = ActorRef[Ack.type]

          Await.result(edgei1h1 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h1 ? (AddOutput(hiddenLayer1, _: ack)), d)

          Await.result(edgei1h2 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h2 ? (AddOutput(hiddenLayer2, _: ack)), d)

          Await.result(edgei2h1 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h1 ? (AddOutput(hiddenLayer1, _: ack)), d)

          Await.result(edgei2h2 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h2 ? (AddOutput(hiddenLayer2, _: ack)), d)

          Await.result(edgei3h1 ? (AddInput(inputLayer3, _: ack)), d)
          Await.result(edgei3h1 ? (AddOutput(hiddenLayer1, _: ack)), d)

          Await.result(edgei3h2 ? (AddInput(inputLayer3, _: ack)), d)
          Await.result(edgei3h2 ? (AddOutput(hiddenLayer2, _: ack)), d)

          //Hidden layer to output layer edges.
          Await.result(edgeh1o1 ? (AddInput(hiddenLayer1, _: ack)), d)
          Await.result(edgeh1o1 ? (AddOutput(outputLayer, _: ack)), d)

          Await.result(edgeh2o1 ? (AddInput(hiddenLayer2, _: ack)), d)
          Await.result(edgeh2o1 ? (AddOutput(outputLayer, _: ack)), d)

          //Output layer to printer.
          Await.result(edgeo1p1 ? (AddInput(outputLayer, _: ack)), d)
          Await.result(edgeo1p1 ? (AddOutput(printer, _: ack)), d)

          //Linking edges to nodes.
          Await.result(inputLayer1 ? (AddOutputs(Seq(edgei1h1, edgei1h2), _: ack)), d)
          Await.result(inputLayer2 ? (AddOutputs(Seq(edgei2h1, edgei2h2), _: ack)), d)
          Await.result(inputLayer3 ? (AddOutputs(Seq(edgei3h1, edgei3h2), _: ack)), d)

          Await.result(hiddenLayer1 ? (AddInputs(Seq(edgei1h1, edgei2h1, edgei3h1), _: ack)), d)
          Await.result(hiddenLayer1 ? (AddOutputs(Seq(edgeh1o1), _: ack)), d)

          Await.result(hiddenLayer2 ? (AddInputs(Seq(edgei1h2, edgei2h2, edgei3h2), _: ack)), d)
          Await.result(hiddenLayer2 ? (AddOutputs(Seq(edgeh2o1), _: ack)), d)

          Await.result(outputLayer ? (AddInputs(Seq(edgeh1o1, edgeh2o1), _: ack)), d)
          Await.result(outputLayer ? (AddOutputs(Seq(edgeo1p1), _: ack)), d)

          Await.result(printer ? (AddInputs(Seq(edgeo1p1), _: ack)), d)

          scala.io.Source.fromFile("src/main/resources/data.csv")
            .getLines()
            .foreach{ l =>
              val splits = l.split(",")

              inputLayer1 ! Input(splits(0).toDouble)
              inputLayer2 ! Input(splits(1).toDouble)
              inputLayer3 ! Input(splits(2).toDouble)
            }

          Same
        case Sig(_, Terminated(ref)) =>
          Stopped
      }

    val system = ActorSystem("akka", Props(main))
  }
}
