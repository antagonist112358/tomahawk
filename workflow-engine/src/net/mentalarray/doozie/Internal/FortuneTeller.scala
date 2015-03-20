package net.mentalarray.doozie.Internal

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util._

trait FortuneTeller[T] {

  implicit lazy val ec = ThreadPool.context

  // Helper method for async execution
  protected def async[U](fn : => T) : Future[T] = future(fn)

  // Helper method for processing all the tasks and their results
  protected def awaitAllFutures[A](futures: Traversable[Future[A]]): Try[Traversable[A]] = {

    var outputValues = List.empty[A]

    futures.foreach(f => {
      // Wait for future to finish
      Await.ready(f, Duration.Inf)

      f.value.get match {
        case Success(s) => outputValues ::= s
        case Failure(f) => Failure(f)
      }

    })

    Success(outputValues)
  }

}
