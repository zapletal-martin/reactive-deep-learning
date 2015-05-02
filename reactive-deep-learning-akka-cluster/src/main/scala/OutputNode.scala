import java.text.SimpleDateFormat
import java.util.Date
import Node.WeightedInput

object OutputNode {
  val shardName: String = "OutputNode"
}

class OutputNode() extends HasInputs {
  var i = 0
  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  override def receive = run orElse addInput

  def run: Receive = {
    case WeightedInput(_, a, w) => {
      val time = new Date(System.currentTimeMillis())
      println(s"OUTPUT FROM ${sender().path.name}")
      println(s"Output $i with result ${a} in ${format.format(time)}")
      i = i + 1
    }
  }
}