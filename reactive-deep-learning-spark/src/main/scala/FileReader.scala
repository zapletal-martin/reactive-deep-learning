import akka.actor.Actor
import org.apache.spark.streaming.receiver.ActorHelper
import concurrent.duration._
import scala.reflect.ClassTag

class FileReader[T: ClassTag] extends Actor with ActorHelper {
  import context.dispatcher
  val tick = context.system.scheduler.schedule(5 seconds, 1 hour, self, "tick")

  override def receive: Receive = {
    case "tick" => {
      scala.io.Source.fromFile("src/main/resources/data.csv")
        .getLines()
        .foreach{ l =>
          store(l.asInstanceOf[T])
        }
    }
  }
}
