name := "snippets"

scalaVersion := "2.11.6"

val akkaVersion = "2.3.9"
val eventuateVersion = "0.2-SNAPSHOT"
val akkaStreamsVersion = "1.0-RC1"
val sparkVersion = "1.3.1"

resolvers ++= Seq(
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "OJO Snapshots" at "https://oss.jfrog.org/oss-snapshot-local",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-typed-experimental" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.github.patriknw" %% "akka-data-replication" % "0.11",
  "com.rbmhtechnology" %% "eventuate" % eventuateVersion,
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStreamsVersion,
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaStreamsVersion,
  "com.typesafe.akka" % "akka-http-scala-experimental_2.11" % akkaStreamsVersion,
  "org.scalanlp" %% "breeze" % "0.11.2",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
