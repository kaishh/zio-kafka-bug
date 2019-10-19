import zio._
import zio.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ZioIncoherentInterruptAsyncRepro extends zio.App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val test =
      ZIO.effectAsync[ZEnv, Nothing, Unit](k =>
        Future {
          Thread.sleep(200L)
          k(console.putStrLn("INCOHERENT CONTINUE"))
        })
        .ensuring(console.putStrLn("Async finalizer!") *> ZIO.never)
        .ensuring(console.putStrLn("CAN'T HAPPEN!"))

    test.fork
      .flatMap(_.interrupt.delay(50.millis))
      .as(0)
  }
}
