name := "reactive-deep-learning-spark"

version := "1.0"

scalaVersion := "2.10.4"

val sparkVersion = "1.3.1"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
