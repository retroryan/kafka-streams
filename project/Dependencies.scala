import sbt._

object Dependencies {

  val logbackVersion        = "1.2.3"
  val scalaLoggingVersion   = "3.9.2"
  val scalaTestVersion      = "3.1.0"
  val typesafeConfigVersion = "1.3.4"

  val akkaVersion                     = "2.6.4"
  val alpakkaVersion                  = "1.1.2"
  val scalikeVersion                  = "0.13.0"

  val jsonAssertVersion = "0.0.5"

  val CommonsIOVersion          = "2.4"
  val AlpakkaKafkaVersion       = "1.1.0"

  val logbackclassic = "ch.qos.logback"             % "logback-classic" % logbackVersion
  val scalalogging   = "com.typesafe.scala-logging" %% "scala-logging"  % scalaLoggingVersion
  val scalatest      = "org.scalatest"              %% "scalatest"      % scalaTestVersion % Test
  val typesafeConfig = "com.typesafe"               % "config"          % typesafeConfigVersion

  val akkaActorTyped           = "com.typesafe.akka"            %% "akka-actor-typed"                    % akkaVersion
  val akkaStream               = "com.typesafe.akka"            %% "akka-stream"                         % akkaVersion
  val akkaSlf4j                = "com.typesafe.akka"            %% "akka-slf4j"                          % akkaVersion
  val alpakkaFile              = "com.lightbend.akka"           %% "akka-stream-alpakka-file"            % alpakkaVersion
  val alpakkaCsv               = "com.lightbend.akka"           %% "akka-stream-alpakka-csv"             % alpakkaVersion
  val akkaKafkaStream                 = "com.typesafe.akka"             %% "akka-stream-kafka" % AlpakkaKafkaVersion
  val akkaStreamTyped                 = "com.typesafe.akka"             %% "akka-stream-typed" % akkaVersion

  def commonDeps: Seq[ModuleID] =
    Seq(
      logbackclassic,
      scalalogging,
      scalatest,
      typesafeConfig,
    )

  def akkaDeps: Seq[ModuleID] =
    Seq(
      akkaActorTyped,
      akkaStream,
      akkaSlf4j,
      alpakkaFile,
      alpakkaCsv,
      akkaKafkaStream,
      akkaStreamTyped)
}
