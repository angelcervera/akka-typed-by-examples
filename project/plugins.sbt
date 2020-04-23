addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.0")

// gRPC
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.8.4")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.5") // ALPN agent

// Lightbend Telemetry
addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.13.3")
