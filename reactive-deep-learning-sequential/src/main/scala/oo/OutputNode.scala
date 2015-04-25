package oo

import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date

import oo.Node.WeightedInput

class OutputNode extends HasInputs {
  var i = 0
  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  override def run(in: WeightedInput): Unit = {
    val time = new Date(System.currentTimeMillis())
    println(s"Output $i with result ${in.feature} in ${format.format(time)}")
    i = i + 1
  }
}
