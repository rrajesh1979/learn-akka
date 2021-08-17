name := "learn-akka"

version := "0.1"

scalaVersion := "2.13.6"

val akkaVersion = "2.6.15"
val akkaHTTPVersion = "10.2.6"
lazy val leveldbVersion = "0.12"
lazy val leveldbjniVersion = "1.8"
lazy val protobufVersion = "3.17.3"

resolvers += Resolver.jcenterRepo

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

  "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "3.0.2",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHTTPVersion,

  "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "3.0.2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.1.1",
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "3.0.2",

  "com.typesafe.akka"          %% "akka-persistence" % akkaVersion,

  // local levelDB stores
  "org.iq80.leveldb"            % "leveldb"          % leveldbVersion,
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % leveldbjniVersion,

  // Google Protocol Buffers
  "com.google.protobuf" % "protobuf-java"  % protobufVersion,

)
