package replication

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.Props
import replication.Node.WeightedInput

object OutputNode {
  def props(): Props = Props[OutputNode]
}

class OutputNode() extends HasInputs {
  var i = 0
  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  override def receive = run orElse addInput

  def run: Receive = {
    case WeightedInput(a, w) => {
      val time = new Date(System.currentTimeMillis())
      println(s"Output $i with result ${a} in ${format.format(time)}")
      i = i + 1
    }
  }
}