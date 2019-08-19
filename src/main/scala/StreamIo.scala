import java.time.OffsetDateTime
import java.util.UUID

import cats.effect._
import cats.implicits._
import fs2.Stream
import fs2.kafka._

object StreamIo extends IOApp {
  val producerSettings: ProducerSettings[IO, String, String] =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("http://localhost:9092")
      .withParallelism(1000)

  def run(args: List[String]): IO[ExitCode] = {
    val ioRecord = ProducerRecords.one(ProducerRecord("notifications-io-" + UUID.randomUUID().toString, "", "some io data"))

    Stream(ioRecord)
        .repeat
        .through(produce(producerSettings))
        .chunkN(5000)
        .evalMap(_ => IO { println(s"${OffsetDateTime.now()} -> Processed batch of 5.000 items") })
        .compile
        .drain
        .as(ExitCode.Success)
  }
}
