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

package net.mentalarray.doozie.DSL

/**
 * Tasks is an alias used to represent a collection of Tasks, compiled into a List[Task] representation.
 * @usecase Example usage is like -
 *          var listOfTasks = Tasks(
 *            Task1,
 *            Task2,
 *            Task3,
 *            ...,
 *            Taskn)
 * @note Tasks collections can be executed as a single task.
 */
object Tasks {

  /**
   * Creates a new instance of TaskCollection from an ensemble of WorkflowTasks.
   * @param tasks One or more WorkflowTask instances, separated by commas.
   * @return An executable WorfklowTask representation of the TaskCollection.
   */
  def apply(tasks: WorkflowTask*) = {
    val tasksCol = new RunTaskSequenceTask
    tasks.foreach(tasksCol.addTask(_))
    tasksCol.asInstanceOf[WorkflowTask]
  }

  /**
   * Creates a new instance of TaskCollection from an ensemble of WorkflowTasks, allowing for the configuration of
   * the TaskCollection (as a configurator), which can alter the execution of the ensemble.
   * @param cfgFn The configurator used to alter the configuration of the TaskCollection.
   * @param tasks One or more WorkflowTask instances, separated by commas.
   * @return An executable WorfklowTask representation of the TaskCollection.
   */
  def apply(cfgFn: RunTaskSequenceTask => Unit)(tasks: WorkflowTask*) = {
    val tasksCol = new RunTaskSequenceTask
    cfgFn(tasksCol)
    tasks.foreach(tasksCol.addTask(_))
    tasksCol.asInstanceOf[WorkflowTask]
  }

}
