import Dependencies._


lazy val kafkaService = project
  .in(file("kafka-service"))
  .settings(
    name := "kafka-service",
    libraryDependencies ++= commonDeps ++ akkaDeps,
  )

addCommandAlias("pr", "kafkaService/runMain grandcloud.StreamsSampleProducer")
