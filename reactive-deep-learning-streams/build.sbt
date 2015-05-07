name := "reactive-deep-learning-streams"

version := "1.0"

scalaVersion := "2.11.6"

val akkaStreamsVersion = "1.0-RC1"

resolvers ++= Seq(
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStreamsVersion,
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaStreamsVersion,
  "com.typesafe.akka" % "akka-http-scala-experimental_2.11" % akkaStreamsVersion,
  "org.scalanlp" %% "breeze" % "0.11.2"
)