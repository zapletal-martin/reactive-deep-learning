import java.text.SimpleDateFormat
import java.util.Date

import Node.{NodeMessage, WeightedInput}
import akka.typed.{Behavior, ActorRef, Props}
import akka.typed.ScalaDSL._

object OutputNode  {
  import HasInputs._

  def props() = Props(receive)

  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  def receive = addInput(run(_, 0))

  def run(inputs: Seq[ActorRef[Nothing]], i: Int): Behavior[NodeMessage] = Partial[NodeMessage] {
    case WeightedInput(f, _) =>
      val time = new Date(System.currentTimeMillis())
      println(s"Output $i with result $f in ${format.format(time)}")
      run(inputs, i + 1)
  }
}