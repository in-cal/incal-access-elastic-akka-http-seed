organization := "org.in-cal"

name := "incal-access-elastic-akka-http-seed"

version := "0.0.2"

scalaVersion := "2.11.12" // or "2.12.10"

resolvers ++= Seq(
  Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.7",                          // Akka HTTP (successor of Spray)
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",                        // Akka streaming
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",               // JSON marshalling
  "org.in-cal" %% "incal-access-elastic" % "0.2.4",                       // Elastic search
  "net.codingwell" %% "scala-guice" % "4.0.1",                            // Guice
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",              // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.3"                          // Logging
)

mainClass in assembly := Some("org.incal.access.elastic.seedapp.SeedAppServer")
