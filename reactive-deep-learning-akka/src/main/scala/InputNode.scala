import Node.{NodeMessage, Input}
import akka.typed.Props
import akka.typed.ScalaDSL.{Or, Static}

object InputNode extends HasOutputs {
  def props() = Props(receive)

  def receive = Or(run, addOutput)

  def run = Static[NodeMessage] {
    case i: Input =>
    outputs.foreach(_ ! i)
  }
}