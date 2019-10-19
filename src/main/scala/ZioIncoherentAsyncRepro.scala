import zio._
import zio.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ZioIncoherentAsyncRepro extends zio.App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val test =
      ZIO.effectAsyncMaybe[ZEnv, Nothing, Unit] {
        k =>
          Future {
            Thread.sleep(200L)
            k(console.putStrLn("INCOHERENT CONTINUE"))
          }
          Some(IO.unit)
      }
        .flatMap {
          _ =>
            ZIO.sleep(500.millis) *>
              console.putStrLn("SUCCESS") *>
              ZIO.never
        }
        .ensuring(console.putStrLn("CAN'T HAPPEN!"))
        .uninterruptible

    test.as(0).delay(1.second)
  }
}
