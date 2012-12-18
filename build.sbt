organization := "kv"

name := "queued"

version := "0.2.0"

scalaVersion := "2.9.2"

// Publish in internal repository for now.
publishTo := Some(Resolver.file("repository", new File("/var/www/repository")))

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.4.1",
  "com.novus" %% "salat" % "1.9.1",
  "redis.clients" % "jedis" % "2.0.0",
  "com.typesafe.akka" % "akka-actor" % "2.0.4",
  "com.typesafe.akka" % "akka-kernel" % "2.0.4",
  "com.typesafe.akka" % "akka-remote" % "2.0.4",
  "com.typesafe.akka" % "akka-slf4j" % "2.0.4",
  "com.typesafe.akka" % "akka-testkit" % "2.0.4" % "test",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test,it",
  "org.mockito" % "mockito-core" % "1.9.0" % "test",
  "org.clapper" %% "avsl" % "0.4"
)

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")


