name := "zio-kafka-bug"

scalaVersion := "2.12.9"

fork in run := true

libraryDependencies ++= Seq(
//  "dev.zio"       %% "zio"              % "1.0.0-RC12-1",
  "dev.zio"       %% "zio"              % "1.0.0-RC99",
  "dev.zio"       %% "zio-interop-cats" % "2.0.0.0-RC99",
  "com.ovoenergy" %% "fs2-kafka"        % "0.20.1"
)
