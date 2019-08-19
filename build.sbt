name := "zio-kafka-bug"

scalaVersion := "2.12.9"

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio-interop-cats" % "2.0.0.0-RC2",
  "dev.zio"       %% "zio"              % "1.0.0-RC11-1",
  "com.ovoenergy" %% "fs2-kafka"        % "0.20.0-M2"
)
