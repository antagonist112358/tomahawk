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
 * This class allows for a WorkflowTask that enables execution of scala code in a deferred manner when
 * called upon in the workflow map.
 */

package net.mentalarray.doozie.Tasks

/**
 * Created by bgilcrease on 10/8/14.
 */
class ScalaTask(name: String) extends WorkflowTask(name) with TaskHasResult[String] {

  private var _runJob: (Unit => Boolean) = null

  // Getters
  def runJob : (Unit => Boolean) = _runJob
  
  // Setters
  def run( fn: Unit => Boolean) = {
    _runJob = fn
  }

  override def validate: Unit = {
    if ( runJob == null )
      throw new WorkflowStateException(this, "The run function must be specified.")
  }

}

object ScalaTask {
  def apply(cfgFn: => Boolean): ScalaTask = {
    val task = new ScalaTask("ScalaTask")
    task.run(_ => cfgFn)
    task
  }
}