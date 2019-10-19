name := "zio-kafka-bug"

scalaVersion := "2.12.10"

fork in run := true

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio"              % "1.0.0-RC15",
  "dev.zio"       %% "zio-interop-cats" % "2.0.0.0-RC6",
  "com.ovoenergy" %% "fs2-kafka"        % "0.20.1"
)
