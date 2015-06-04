package akka

import Edge.EdgeMessage
import HasInputs._
import HasOutputs._
import Neuron._
import akka.Node._
import _root_.Edge.EdgeMessage
import akka.typed.ActorRef
import akka.typed.ActorRef
import akka.typed.Behavior
import akka.typed.Props
import akka.typed.ScalaDSL.Partial

import scala.math._

object Akka {

}

object Node {
  trait NodeMessage
  case class Input(feature: Double) extends NodeMessage with EdgeMessage
  case class WeightedInput(feature: Double, weight: Double) extends NodeMessage
  case class UpdateBias(bias: Double) extends NodeMessage

  case object Ack
  case class AddInputs(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NodeMessage
  case class AddOutputs(outputs: Seq[ActorRef[Input]], replyTo: ActorRef[Ack.type]) extends NodeMessage
}

object Neuron {
  val simple: Double => Double = x => if(x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}

object Node {
  trait NodeMessage
  case class Input(feature: Double) extends NodeMessage with EdgeMessage
  case class WeightedInput(feature: Double, weight: Double) extends NodeMessage
  case class UpdateBias(bias: Double) extends NodeMessage

  case object Ack
  case class AddInputs(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NodeMessage
  case class AddOutputs(outputs: Seq[ActorRef[Input]], replyTo: ActorRef[Ack.type]) extends NodeMessage
}

object HasInputs {
  def addInput[T](behavior: Seq[ActorRef[Nothing]] =>  Behavior[T]) = Partial[T] {
    case AddInputs(i, r) =>
      r ! Ack
      behavior(i)
  }
}

object HasOutputs {
  def addOutput[T](behavior: (Seq[ActorRef[Nothing]], Seq[ActorRef[Input]]) =>  Behavior[T], inputs: Seq[ActorRef[Nothing]]) = Partial[T] {
    case AddOutputs(o, r) =>
      r ! Ack
      behavior(inputs, o)
  }
}

object Perceptron {

  def props() = Props(behaviour)
  def behaviour = addInput(addOutput(feedForward(_, _, 0.2, sigmoid, Vector(), Vector()), _))

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[ActorRef[Nothing]]) =
    w.length == in.length && f.length == in.length

  def feedForward(
      inputs: Seq[ActorRef[Nothing]],
      outputs: Seq[ActorRef[Input]],
      bias: Double,
      activationFunction: Double => Double,
      weightsT: Vector[Double],
      featuresT: Vector[Double]): Behavior[NodeMessage] = Partial[NodeMessage] {

    case WeightedInput(f, w) =>
      val featuresTplusOne = featuresT :+ f
      val weightsTplusOne = weightsT :+ w

      if (allInputsAvailable(featuresTplusOne, weightsTplusOne, inputs)) {
        val activation = activationFunction(weightsTplusOne.zip(featuresTplusOne).map(x => x._1 * x._2).sum + bias)
        outputs.foreach(_ ! Input(activation))

        feedForward(inputs, outputs, bias, activationFunction, Vector(), Vector())
      } else {
        feedForward(inputs, outputs, bias, activationFunction, weightsTplusOne, featuresTplusOne)
      }
  }
}



 /*   case UpdateBias(newBias) =>
      feedForward(inputs, outputs, newBias, activationFunction, weightsT, featuresT)
  }*/
