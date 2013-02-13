organization := "kv"

name := "queued"

version := "0.3.1"

scalaVersion := "2.10.0"

// Publish in internal repository for now.
publishTo := Some(Resolver.file("repository", new File("/var/www/repository")))

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah-core" % "2.5.0",
  "org.mongodb" %% "casbah-query" % "2.5.0",
  "com.novus" %% "salat-core" % "1.9.2-SNAPSHOT",
  "redis.clients" % "jedis" % "2.0.0",
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "com.typesafe.akka" %% "akka-kernel" % "2.1.0",
  "com.typesafe.akka" %% "akka-remote" % "2.1.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.1.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test,it",
  "org.mockito" % "mockito-core" % "1.9.0" % "test",
  "org.clapper" %% "avsl" % "1.0.1"
)

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

