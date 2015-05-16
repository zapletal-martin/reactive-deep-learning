name := "reactive-deep-learning-akka-cluster"

version := "1.0"

scalaVersion := "2.11.6"

val akkaVersion = "2.3.9"

resolvers ++= Seq(
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "patriknw at bintray" at "http://dl.bintray.com/patriknw/maven"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.github.patriknw" %% "akka-data-replication" % "0.11",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.7",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
