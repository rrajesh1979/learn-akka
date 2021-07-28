name := "learn-akka"

version := "0.1"

scalaVersion := "2.13.6"

val akkaVersion = "2.6.15"

libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,

  // https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,

  // https://mvnrepository.com/artifact/org.scalatest/scalatest
  "org.scalatest" %% "scalatest" % "3.2.9",

  "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.0",

  // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
  "org.slf4j" % "slf4j-api" % "1.7.32",
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
  "org.slf4j" % "slf4j-simple" % "1.7.32",

)
