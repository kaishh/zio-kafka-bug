import zio._
import zio.console.Console

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import zio.duration._

object ZioIncoherentAsyncRepro extends zio.App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val test =
      ZIO.effectAsyncMaybe[Environment, Nothing, Unit] {
        k =>
          Future {
            Thread.sleep(200L)
            k(console.putStrLn("INCOHERENT CONTINUE"))
          }
          Some(IO.unit)
      }
        .flatMap(_ => ZIO.sleep(500.millis))
        .ensuring(console.putStrLn("CAN'T HAPPEN!"))
        .uninterruptible

    test.as(0).delay(1.second)
  }
}
