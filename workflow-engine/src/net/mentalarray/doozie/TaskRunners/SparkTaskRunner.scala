/*
 Copyright 2014 MentalArray, LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/**
 * This class allows for a WorkflowTask that enables configuration of spark code so that SparkTask can add
 * the code supplied to the workflow map so that it can be executed in a deferred manner.
 */

package net.mentalarray.doozie.TaskRunners

import net.mentalarray.doozie.Internal.SparkRunner
import net.mentalarray.doozie.Tasks.SparkTask

/**
 * Created by bgilcrease on 11/4/14.
 */
class SparkTaskRunner extends WorkflowTaskRunner with Logging {
  override type T = SparkTask

  // Impl
  override protected def doAction(state: T): Boolean = {
    SparkRunner.addArgs(state.getArrayOfArgs)
    SparkRunner.run
  }

  override protected def doTest(state: T): Unit = {

  }

}
