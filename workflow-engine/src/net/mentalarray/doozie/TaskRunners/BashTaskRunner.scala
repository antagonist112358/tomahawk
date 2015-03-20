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
 * This class allows for a WorkflowTask that enables CLI execution of a string
 */

package net.mentalarray.doozie.TaskRunners

import scala.sys.process._

/**
 * Created by kdivincenzo on 9/9/14.
 */
class BashTaskRunner extends WorkflowTaskRunner with Logging {

  type T = BashTask

  // Impl
  override protected def doAction(state: BashTask): Boolean = {

    val cmd = state.command

    val out = new StringBuilder
    Log debug("Shell command which will be executed: %s" format cmd)
    val resultCode = cmd ! ProcessLogger((line: String) => out.append(line + '\n'))

    state.setResult(out.toString())

    resultCode == 0

  }

  override protected def doTest(state: BashTask): Unit = {

    try {
      state.validate
    } catch {
      case ex: Throwable => Log error("Invalid task state: %s" format ex.getMessage)
    }

    Log info("Shell command which will be executed: %s" format state.command)
  }
}
