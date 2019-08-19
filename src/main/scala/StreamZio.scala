import java.time.Instant
import java.util.UUID

import fs2.Stream
import fs2.kafka.{ProducerRecord, ProducerRecords, ProducerSettings, produce}
import zio.clock.Clock
import zio.interop.catz._
import zio._

object StreamZio extends App {
  override def run(args: List[String]) =
    (for {
      rt <- ZIO.runtime[Clock]
      _ <- startStream(rt)
    } yield ()).fold(_ => 1, _ => 0)

  private val producerSettings = ProducerSettings[Task, String, String]
    .withBootstrapServers("http://localhost:9092")

  private def startStream(implicit runtime: Runtime[Clock]): Task[Unit] = {
    val zioRecord = ProducerRecords.one(ProducerRecord("notifications-zio-" + UUID.randomUUID().toString, "", "some zio data"))

    Stream(zioRecord).repeat
      .through(produce(producerSettings))
      .chunkN(5000)
      .evalMap(_ => Task { println(s"${Instant.now()} -> Processed batch of 5.000 items") })
      .compile
      .drain
  }
}
