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
 * This class allows for a WorkflowTask that enables configuration of ScalaTask so that scala code
 * can be run in a deferred manner when called upon in the workflow map.
 */

package net.mentalarray.doozie.TaskRunners

/**
 * Created by bgilcrease on 10/8/14.
 */

class ScalaTaskRunner  extends WorkflowTaskRunner with Logging {
  override type T = ScalaTask

  // Impl
  override protected def doAction(state: T): Boolean = {

    try {

      Log debug ("Scala command[s] which will be executed: %s" format(state.runJob.toString()))

      val result = state.runJob()

    } catch {
      case ex: Exception => {
        Log error("Scala task had exception executing code: %s" format(state.runJob.toString), ex)
        throw ex
      }
    }


    // Success
    true
  }

  override protected def doTest(state: T): Unit = {
    try {
      Log info "Validating task state..."
      state.validate
    } catch {
      case ex: Throwable => Log error("Invalid task state: %s" format(ex.getMessage))
    }
  }
}

