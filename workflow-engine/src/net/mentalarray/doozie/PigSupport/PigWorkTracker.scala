package net.mentalarray.doozie.PigSupport

import java.util.UUID

import net.mentalarray.doozie.Tasks.PigTask

import scala.collection.concurrent
import scala.concurrent.Future


/**
 * Created by kdivincenzo on 2/18/15.
 */
protected[PigSupport] class PigWorkTracker {

  // Work registery
  private val workRegistry = concurrent.TrieMap.empty[UUID, PigWorkItem]

  def registerWork(task: PigTask) : (UUID, Future[Boolean]) = {
    val workItem = new PigWorkItem(task)
    val id = workItem.id
    workRegistry += id -> workItem
    (id, workItem.workFuture)
  }

  def tryGetWorkForID(id: UUID) : Option[PigTask] = workRegistry.get(id) match {
    case Some(workItem) => Some(workItem.pigTask)
    case None => None
  }

  def workCompleted(id: UUID, result: Either[Boolean, Exception]): Unit = {
    val workItem = workRegistry(id)
    result match {
      case Left(didPass) => workItem handleWorkCompletedResult didPass
      case Right(error) => workItem handleWorkCompletedResult error
    }
  }

  def removeWork(id: UUID) = workRegistry.remove(id)
}
