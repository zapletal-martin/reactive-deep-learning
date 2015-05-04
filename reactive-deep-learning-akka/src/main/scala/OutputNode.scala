import java.text.SimpleDateFormat
import java.util.Date

import Node.{NodeMessage, WeightedInput}
import akka.typed.Props
import akka.typed.ScalaDSL.{Or, Static}

object OutputNode extends HasInputs {
  def props() = Props(receive)

  var i = 0
  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  def receive = Or(run, addInput)

  def run = Static[NodeMessage] {
    case WeightedInput(f, _) =>
      val time = new Date(System.currentTimeMillis())
      println(s"Output $i with result $f in ${format.format(time)}")
      i = i + 1
  }
}