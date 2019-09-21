name := "zio-kafka-bug"

scalaVersion := "2.12.9"

fork in run := true

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio-interop-cats" % "2.0.0.0-RC3",
  "dev.zio"       %% "zio"              % "1.0.0-SNAPSHOT",
  "com.ovoenergy" %% "fs2-kafka"        % "0.20.1"
)
