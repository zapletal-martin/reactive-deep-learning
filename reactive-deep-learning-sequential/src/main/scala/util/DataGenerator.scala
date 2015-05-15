package util

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat

object DataGenerator extends App {

  override def main (args: Array[String]) = {
    val f = new DecimalFormat("###.####");

    Files.write(
      Paths.get("src/main/resources/data2.csv"),
      (0d to 10 by 0.0001).map(f.format(_)).map(x => s"$x,$x,$x").mkString("\r\n").getBytes(StandardCharsets.UTF_8))
  }
}
