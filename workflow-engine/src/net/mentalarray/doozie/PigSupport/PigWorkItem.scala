package net.mentalarray.doozie.PigSupport

import java.util.UUID

import scala.concurrent.Promise

/**
 * Created by kdivincenzo on 2/18/15.
 */
protected[PigSupport] class PigWorkItem(task: PigTask) {

  // The work ID
  private val workId = UUID.randomUUID
  def id : UUID = workId

  // The PigTask
  def pigTask = task

  // The promise for the work item
  private val workPromise = Promise[Boolean]

  // Future for the work promise
  def workFuture = workPromise.future

  // Handle work completed results
  def handleWorkCompletedResult(result: Boolean) = workPromise success result
  def handleWorkCompletedResult(ex: Exception) = workPromise failure ex
}