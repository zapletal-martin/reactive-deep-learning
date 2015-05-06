object Bug {
  val parallelModels = Array(
    3,
    3,
    3
  )

  val file = scala.io.Source.fromFile("src/main/resources/data.csv").getLines()

  println(s"file size ${file.size.toDouble}")
  println(s"model size ${parallelModels.size.toDouble}")

  val fileSize: Double = file.size.toDouble
  val modelSize: Double = parallelModels.size.toDouble
  val divided: Double = fileSize./(modelSize)

  println(s"divided $divided")
}
