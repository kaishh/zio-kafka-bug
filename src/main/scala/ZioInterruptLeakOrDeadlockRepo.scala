import java.util.concurrent.atomic.LongAdder

import zio.{UIO, ZEnv, _}

object ZioInterruptLeakOrDeadlockRepo extends zio.App {

  val startedCounter = new LongAdder
  val completedCounter = new LongAdder
  val awakeCounter = new LongAdder
  val pendingGauge = new LongAdder

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val leakOrDeadlockTest = for {
      _ <- UIO {
        startedCounter.increment()
        pendingGauge.increment()
      }

      sleepInterruptFiber <- IO.never.fork

      // This blocks / leaks once every 20,000 rounds or so
      _ <- sleepInterruptFiber.interrupt
        .ensuring(UIO {
          awakeCounter.increment()
          pendingGauge.decrement()
        })

      _ <- UIO(completedCounter.increment())
    } yield ()

    val main = for {
      _ <-
        ZIO.runtime[Any].map(_.Platform.executor.metrics.get)
          .flatMap {
            metrics => UIO(new Thread(() => {
              while (true) {
                println(s"started=${startedCounter.longValue()} awake=${awakeCounter.longValue()} completed=${completedCounter.longValue()} pending=${pendingGauge.longValue()} queued=${metrics.size}")
                Thread.sleep(1000L)
              }
            }).start())
          }

      _ <- leakOrDeadlockTest.forever
    } yield 0

    main
  }
}
