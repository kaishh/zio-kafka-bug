import cats.data.OptionT
import zio._
import zio.interop.catz._
import zio.syntax._

object TestTraceOfOptionT extends zio.App {

  def x(z: Any => Task[Int]): OptionT[Task, Int] = OptionT.liftF[Task, Int](1.succeed).flatMap(_ => y(z))
  def y(z: Any => Task[Int]): OptionT[Task, Int] = OptionT[Task, Int](ZIO.some(()).flatMap {
    case Some(value) => z(value).map(Some(_))
    case None => ZIO.none
  })
  def z: Any => Task[Int] = _ => throw new RuntimeException

  override def run(args: List[String]): UIO[Int] = {
    x(z).getOrElse(0).orDie
  }
}
