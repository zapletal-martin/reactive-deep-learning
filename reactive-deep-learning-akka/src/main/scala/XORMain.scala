import Node._
import Edge.{UpdateWeight, AddInput, AddOutput}
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.typed._
import akka.typed.ScalaDSL._
import akka.typed.AskPattern._

object XORMain extends App {

  //**************************************
  //      H
  //
  // I    H
  //
  //      H     O
  //
  // I    H
  //
  //      H
  //**************************************
  override def main (args: Array[String]) {
    val main: Behavior[Unit] =
      Full {
        case Sig(ctx, PreStart) =>

          //Input layer nodes.
          val inputLayer1 = ctx.spawn(InputNode.props(), "inputLayer1")
          val inputLayer2 = ctx.spawn(InputNode.props(), "inputLayer2")

          //Hidden layer nodes.
          val hiddenLayer1 = ctx.spawn(Perceptron.props(), "hiddenLayer1")
          val hiddenLayer2 = ctx.spawn(Perceptron.props(), "hiddenLayer2")
          val hiddenLayer3 = ctx.spawn(Perceptron.props(), "hiddenLayer3")
          val hiddenLayer4 = ctx.spawn(Perceptron.props(), "hiddenLayer4")
          val hiddenLayer5 = ctx.spawn(Perceptron.props(), "hiddenLayer5")

          //Output layer nodes.
          val outputLayer = ctx.spawn(Perceptron.props(), "outputLayer")

          //Printer.
          val printer = ctx.spawn(OutputNode.props(), "printer")

          //Edges.
          val edgei1h1 = ctx.spawn(Edge.props(), "edgei1h1")
          val edgei1h2 = ctx.spawn(Edge.props(), "edgei1h2")
          val edgei1h3 = ctx.spawn(Edge.props(), "edgei1h3")
          val edgei1h4 = ctx.spawn(Edge.props(), "edgei1h4")
          val edgei1h5 = ctx.spawn(Edge.props(), "edgei1h5")

          val edgei2h1 = ctx.spawn(Edge.props(), "edgei2h1")
          val edgei2h2 = ctx.spawn(Edge.props(), "edgei2h2")
          val edgei2h3 = ctx.spawn(Edge.props(), "edgei2h3")
          val edgei2h4 = ctx.spawn(Edge.props(), "edgei2h4")
          val edgei2h5 = ctx.spawn(Edge.props(), "edgei2h5")

          val edgeh1o1 = ctx.spawn(Edge.props(), "edgeh1o1")
          val edgeh2o1 = ctx.spawn(Edge.props(), "edgeh2o1")
          val edgeh3o1 = ctx.spawn(Edge.props(), "edgeh3o1")
          val edgeh4o1 = ctx.spawn(Edge.props(), "edgeh4o1")
          val edgeh5o1 = ctx.spawn(Edge.props(), "edgeh5o1")

          val edgeo1p1 = ctx.spawn(Edge.props(), "edgeo1p1")


          implicit val t = Timeout(10 seconds)
          val d = t.duration

          type ack = ActorRef[Ack.type]

          Await.result(edgei1h1 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h1 ? (AddOutput(hiddenLayer1, _: ack)), d)
          Await.result(edgei1h2 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h2 ? (AddOutput(hiddenLayer2, _: ack)), d)
          Await.result(edgei1h3 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h3 ? (AddOutput(hiddenLayer3, _: ack)), d)
          Await.result(edgei1h4 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h4 ? (AddOutput(hiddenLayer4, _: ack)), d)
          Await.result(edgei1h5 ? (AddInput(inputLayer1, _: ack)), d)
          Await.result(edgei1h5 ? (AddOutput(hiddenLayer5, _: ack)), d)

          Await.result(edgei2h1 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h1 ? (AddOutput(hiddenLayer1, _: ack)), d)
          Await.result(edgei2h2 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h2 ? (AddOutput(hiddenLayer2, _: ack)), d)
          Await.result(edgei2h3 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h3 ? (AddOutput(hiddenLayer3, _: ack)), d)
          Await.result(edgei2h4 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h4 ? (AddOutput(hiddenLayer4, _: ack)), d)
          Await.result(edgei2h5 ? (AddInput(inputLayer2, _: ack)), d)
          Await.result(edgei2h5 ? (AddOutput(hiddenLayer5, _: ack)), d)

          //Hidden layer to output layer edges.
          Await.result(edgeh1o1 ? (AddInput(hiddenLayer1, _: ack)), d)
          Await.result(edgeh1o1 ? (AddOutput(outputLayer, _: ack)), d)
          Await.result(edgeh2o1 ? (AddInput(hiddenLayer2, _: ack)), d)
          Await.result(edgeh2o1 ? (AddOutput(outputLayer, _: ack)), d)
          Await.result(edgeh3o1 ? (AddInput(hiddenLayer3, _: ack)), d)
          Await.result(edgeh3o1 ? (AddOutput(outputLayer, _: ack)), d)
          Await.result(edgeh4o1 ? (AddInput(hiddenLayer4, _: ack)), d)
          Await.result(edgeh4o1 ? (AddOutput(outputLayer, _: ack)), d)
          Await.result(edgeh5o1 ? (AddInput(hiddenLayer5, _: ack)), d)
          Await.result(edgeh5o1 ? (AddOutput(outputLayer, _: ack)), d)

          //Output layer to printer.
          Await.result(edgeo1p1 ? (AddInput(outputLayer, _: ack)), d)
          Await.result(edgeo1p1 ? (AddOutput(printer, _: ack)), d)

          //Linking edges to nodes.
          Await.result(inputLayer1 ? (AddOutputs(Seq(edgei1h1, edgei1h2, edgei1h3, edgei1h4, edgei1h5), _: ack)), d)
          Await.result(inputLayer2 ? (AddOutputs(Seq(edgei2h1, edgei2h2, edgei2h3, edgei2h4, edgei2h5), _: ack)), d)

          Await.result(hiddenLayer1 ? (AddInputs(Seq(edgei1h1, edgei2h1), _: ack)), d)
          Await.result(hiddenLayer1 ? (AddOutputs(Seq(edgeh1o1), _: ack)), d)
          Await.result(hiddenLayer2 ? (AddInputs(Seq(edgei1h2, edgei2h2), _: ack)), d)
          Await.result(hiddenLayer2 ? (AddOutputs(Seq(edgeh2o1), _: ack)), d)
          Await.result(hiddenLayer3 ? (AddInputs(Seq(edgei1h3, edgei2h3), _: ack)), d)
          Await.result(hiddenLayer3 ? (AddOutputs(Seq(edgeh3o1), _: ack)), d)
          Await.result(hiddenLayer4 ? (AddInputs(Seq(edgei1h4, edgei2h4), _: ack)), d)
          Await.result(hiddenLayer4 ? (AddOutputs(Seq(edgeh4o1), _: ack)), d)
          Await.result(hiddenLayer5 ? (AddInputs(Seq(edgei1h5, edgei2h5), _: ack)), d)
          Await.result(hiddenLayer5 ? (AddOutputs(Seq(edgeh5o1), _: ack)), d)

          Await.result(outputLayer ? (AddInputs(Seq(edgeh1o1, edgeh2o1, edgeh3o1, edgeh4o1, edgeh5o1), _: ack)), d)
          Await.result(outputLayer ? (AddOutputs(Seq(edgeo1p1), _: ack)), d)

          Await.result(printer ? (AddInputs(Seq(edgeo1p1), _: ack)), d)

          hiddenLayer1 ! UpdateBias(-1.159477860560574)
          hiddenLayer2 ! UpdateBias(7.360948031708151)
          hiddenLayer3 ! UpdateBias(-40.119103666289355)
          hiddenLayer4 ! UpdateBias(1.9910680740541433)
          hiddenLayer5 ! UpdateBias(35.36995814832137)

          edgei1h1 ! UpdateWeight(-24.58461081841181)
          edgei1h2 ! UpdateWeight(-24.687630829768242)
          edgei1h3 ! UpdateWeight(-53.19737482058547)
          edgei1h4 ! UpdateWeight(-8.627833533421583)
          edgei1h5 ! UpdateWeight(-10.89544114987001)

          edgei2h1 ! UpdateWeight(-57.122870841850805)
          edgei2h2 ! UpdateWeight(27.09898145911361)
          edgei2h3 ! UpdateWeight(-0.3752614168803033)
          edgei2h4 ! UpdateWeight(2.616234689416054)
          edgei2h5 ! UpdateWeight(1.1951518346473402)

          outputLayer ! UpdateBias(61.48889773699686)

          edgeh1o1 ! UpdateWeight(-52.985568497843346)
          edgeh2o1 ! UpdateWeight(-22.904824971874294)
          edgeh3o1 ! UpdateWeight(-67.1736083116406)
          edgeh4o1 ! UpdateWeight(22.172982906414187)
          edgeh5o1 ! UpdateWeight( -53.706172957900264)

          inputLayer1 ! Input(0)
          inputLayer2 ! Input(0)

          inputLayer1 ! Input(0)
          inputLayer2 ! Input(1)

          inputLayer1 ! Input(1)
          inputLayer2 ! Input(0)

          inputLayer1 ! Input(1)
          inputLayer2 ! Input(1)

          Same
        case Sig(_, Terminated(ref)) =>
          Stopped
      }

    val system = ActorSystem("akka", Props(main))
  }
}
