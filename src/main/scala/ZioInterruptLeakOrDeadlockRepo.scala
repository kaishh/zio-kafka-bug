import java.lang.ref.WeakReference
import java.util.concurrent.atomic.{AtomicBoolean, LongAdder}

import cats.syntax.flatMap
import zio._
import zio.console._
import zio.duration._
import zio.interop.M

import scala.language.reflectiveCalls

object ZioInterruptLeakOrDeadlockRepo extends zio.App {

  val startedCounter = new LongAdder
  val completedCounter = new LongAdder
  val awakeCounter = new LongAdder
  val interruptedCounter = new LongAdder
  val pendingGauge = new LongAdder
  val timeoutCounter = new LongAdder

  def monitoredInterrupt(f: Fiber[_, _]): UIO[_] = {
    f.interrupt
      .ensuring(UIO(interruptedCounter.increment()))
      .fork.flatMap {
      fiber =>
        val fweakRef = new WeakReference(f)
        val weakRef = new WeakReference(fiber)
        val interrupted = new AtomicBoolean(false)

        def monitorThread(w: WeakReference[Fiber[_, _]]) = {
          new Thread(() => {
            var i = 0
            while (!interrupted.get()) {
              if ((weakRef.get() eq null) && !interrupted.get()) {
                System.err.println(
                  s"LEAKED N=${M.count.incrementAndGet()}, WAITER WILL NEVER WAKE UP originalGcd=${
                    fweakRef
                      .get() eq null
                  } waiterGCd=${w.get() eq null} originalInterrupted=${
                    Option(fweakRef.get())
                      .map(_.asInstanceOf[ {var interrupted: Boolean}].interrupted)
                  }"
                )
                throw new Throwable("watcher thread died")
              }
              Thread.sleep(1000L)
              i += 1
            }
            //          println(s"monitor finished in $i attempts")
          }) {
            override def getUncaughtExceptionHandler = (_, _) => ()
          }
        }

        (fiber.await *> UIO(interrupted.set(true))).fork.flatMap {
          waiterFiber =>
//            UIO(monitorThread(new WeakReference(waiterFiber)).start())
            UIO.unit
        } *> fiber.await.ignore
    }
  }

  def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val leakOrDeadlockTest = for {
      _ <- UIO {
        startedCounter.increment()
        pendingGauge.increment()
      }

      sleepInterruptFiber <- clock.sleep(1.minute).fork

      // This blocks / leaks once every 20,000 rounds or so
      _ <- monitoredInterrupt(sleepInterruptFiber)
        .ensuring(UIO {
          awakeCounter.increment()
          pendingGauge.decrement()
        })
        .onInterrupt(UIO(interruptedCounter.increment()))

      _ <- UIO(completedCounter.increment())
    } yield ()

    val main = for {
      _ <-
        ZIO.runtime[Any].map(_.Platform.executor.metrics.get).flatMap {
          metrics =>
            UIO(s"started=${startedCounter.longValue()} awake=${awakeCounter.longValue()} completed=${completedCounter.longValue()} pending=${pendingGauge.longValue()} timed-out=${timeoutCounter.longValue()} interrupted=${interruptedCounter.longValue()} queued=${metrics.size}")
              .flatMap(putStrLn)
              .repeat(ZSchedule.fixed(1.second))
        }.fork

      _ <- ZIO.foreachParN_(8)(1 to 8) {
        _ =>
          leakOrDeadlockTest
            .fork.flatMap(_.join).timeout(10.seconds)
            .repeat(ZSchedule.identity[Option[Unit]].logInput {
              case Some(_) => UIO.unit
              case None => UIO(timeoutCounter.increment())
            })
      }
    } yield 0

    main
  }
}
