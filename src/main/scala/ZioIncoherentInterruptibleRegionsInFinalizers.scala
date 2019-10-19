import zio.console.putStrLn
import zio.duration._
import zio.{URIO, ZIO}

object ZioIncoherentInterruptibleRegionsInFinalizers extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, Int] = {
    ZIO.never.ensuring {
      ZIO.sleep(1.second) *>
      ZIO.unit.interruptible *> // removing this causes print to happen
      putStrLn("Waited 1s") // never printed because interruptible causes immediate interrupt
    }.fork.flatMap {
      ZIO.sleep(1.second) *> _.interrupt
    }.as(0)
  }
}
