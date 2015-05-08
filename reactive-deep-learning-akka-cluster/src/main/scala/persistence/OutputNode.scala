package persistence

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.Props
import persistence.Node.WeightedInput

object OutputNode {
  def props(): Props = Props[OutputNode]
  val shardName: String = "OutputNode"
}

class OutputNode() extends HasInputs {
  override def persistenceId: String = self.path.name

  var i = 0
  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  override def receiveCommand: Receive = run orElse addInput

  override def receiveRecover: Receive = {
    case _ => println(s"Recovering $persistenceId")
  }

  def run: Receive = {
    case WeightedInput(_, a, w) => {
      val time = new Date(System.currentTimeMillis())
      println(s"Output $i with result ${a} in ${format.format(time)}")
      i = i + 1
    }
  }


}