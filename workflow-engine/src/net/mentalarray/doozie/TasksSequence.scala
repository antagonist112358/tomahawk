/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mentalarray.doozie

import scala.collection.mutable

/**
 * An iterable collection of WorkflowTasks.
 */
class TasksSequence extends Seq[WorkflowTask] {

  private val _tasks = mutable.MutableList.empty[WorkflowTask]

  /**
   * Operator '+=' appends a WorkflowTask to an existing TaskSequence.
   * @param nextTask The WorkflowTask to append.
   * @return The current instance of TaskSequence, which has the 'nextTask' appended to it.
   */
  def +=(nextTask: WorkflowTask) : TasksSequence = {
    _tasks += nextTask
    this
  }

  /**
   * Operator '++' concatenates two TaskSequences together.
   * @param taskSeq The TaskSequence to append.
   * @return The current instance of TaskSequence, which has been concatenated with 'taskSeq'.
   */
  def ++(taskSeq: Seq[WorkflowTask]) : TasksSequence = {
    taskSeq.map { _tasks += _ }
    this
  }

  /**
   * The number of items in this TaskSequence.
   * @return The integer number of items in this TaskSequence.
   */
  override def length: Int = _tasks.length

  /**
   * Indexer - used for retrieving items from the TaskSequence by index.
   * @param idx The integer index of the item within the TaskSequence to retrieve.
   * @return The instance of the WorkflowTask at the specified index.
   */
  override def apply(idx: Int): WorkflowTask = _tasks(idx)

  /**
   * An iterator over the TaskSequence.
   * @return The Iterator[WorkflowTask] instance.
   */
  override def iterator: Iterator[WorkflowTask] = _tasks.iterator

  /**
   * TaskSequence foreach extension.
   * @param f A function with takes each element of this TaskSequence to type 'U'.
   * @tparam U The resultant type of each iteration of the foreach function 'f'.
   */
  override def foreach[U](f: (WorkflowTask) => U): Unit = _tasks.foreach(f)
}

/**
 * Companion object for TaskSequence class.
 */
object TasksSequence {

  /**
   * Implicit conversion from a single WorkflowTask into a new TaskSequence of WorkflowTasks.
   * @param nextTask The initial WorkflowTask which will be inserted into a new TaskSequence.
   * @return An instance of TaskSequence.
   */
  implicit def taskToTasks(nextTask: WorkflowTask) : TasksSequence = {
    val tasks = new TasksSequence
    tasks += nextTask
    tasks
  }

  /**
   * Allows for creation of a new TaskSequence from an ensemble of WorkflowTasks.
   * @param tasksList An ensemble of WorkflowTasks, separated by commas.
   * @return
   */
  def apply(tasksList: WorkflowTask*) : TasksSequence = {
    val tasks = new TasksSequence
    tasks ++ tasksList
    tasks
  }

}

