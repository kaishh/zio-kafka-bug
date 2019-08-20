import java.time.OffsetDateTime
import java.util.UUID

import fs2.Stream
import fs2.kafka.{ProducerRecord, ProducerRecords, ProducerSettings, produce}
import zio._
import zio.interop.catz._

object StreamZio extends App {
  private val producerSettings = ProducerSettings[Task, String, String]
    .withBootstrapServers("http://localhost:9092")
    .withParallelism(1000)

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = ZIO.runtime.flatMap { implicit rt: Runtime[Environment] =>
    val zioRecord = ProducerRecords.one(ProducerRecord("notifications-zio-" + UUID.randomUUID().toString, "", "some zio data"))

    Stream(zioRecord)
      .repeat
      .through(produce(producerSettings))
      .chunkN(5000)
      .evalMap[Task, Unit](_ => Task.descriptor.flatMap { f =>
        Task { println(s"${OffsetDateTime.now()} FiberId(${f.id}) -> Processed batch of 5.000 items") }
      })
      .compile
      .drain
      .const(0).orDie
  }
}
