name := "reactive-deep-learning-akka-cluster"

version := "1.0"

scalaVersion := "2.11.6"

val akkaVersion = "2.4-SNAPSHOT"
val eventuateVersion = "0.2-SNAPSHOT"

resolvers ++= Seq(
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "OJO Snapshots" at "https://oss.jfrog.org/oss-snapshot-local"
)

libraryDependencies ++= Seq(
  "com.rbmhtechnology" %% "eventuate" % eventuateVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

fork in run := true
