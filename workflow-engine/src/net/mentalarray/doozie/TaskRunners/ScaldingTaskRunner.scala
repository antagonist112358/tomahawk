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
 * This class allows for a WorkflowTask that enables execution of scalding code in a deferred manner when
 * called upon in the workflow map.
 */

package net.mentalarray.doozie.TaskRunners

import java.io.File

import net.mentalarray.doozie.Tasks.ScaldingTask
import net.mentalarray.doozie.{Logging, WorkflowTaskRunner}
import org.apache.hadoop.util.ToolRunner

import scala.collection.mutable

/**
 * Created by kdivincenzo on 10/2/14.
 */
class ScaldingTaskRunner extends WorkflowTaskRunner with Logging {
  override type T = ScaldingTask

  /**
   *
   * @param inputPath location of scala script with scalding code in it
   * @param className acquire the class name for the job
   * @param jarList list of additional .jar files that are required to run the job
   * @return
   */

  private def configureScalding(inputPath: String, className: String, jarList: Seq[String]) = {
    // Define the clean-up action
    var cleanupAction: Unit => Unit = Unit => { }
    // Set the className, and final jarPath
    var finalClassName = className
    // Resolve all the dependencies
    val jars = resolveJars(jarList)

    // If the input file is a scala file, compile it now
    if (inputPath.endsWith(".scala")) {
      val compiler = new ScalaCompiler

      val jobJarFilename = java.util.UUID.randomUUID.toString + ".jar"
      val jobJarFilePath = Path.combine(Application.currentDirectory, "temp/", jobJarFilename)

      finalClassName = compiler.createJarFromScript(jobJarFilePath, inputPath).getName

      jars += Path.file(jobJarFilePath)

      //cleanupAction = _ => Path.file(jobJarFilePath).delete()
    } else {
      jars += Path.file(inputPath)
      // create an instance of the class (for the loader)
      ClassLoader.loadFile(inputPath)
    }

    // Create the tool
    (ScaldingNoJar.buildTool(jars.toList), finalClassName, cleanupAction)
  }

  private def resolveJars(jarList: Seq[String]) = {
    // Put together a list of Jars (include this jarFile since it has the Scalding depenedencies)
    val jarFiles = mutable.MutableList.empty[File]

    jarList.foreach(fileName => {
      val file = new File(fileName)

      if (file.exists)
        jarFiles += file
      else Path.resolveJarInClassPath(fileName) match {
        case Some(jarFile) => jarFiles += jarFile
        case None => throw new WorkflowException(s"Could not resolve Scaling dependency in classPath: $fileName")
      }
    })

    jarFiles
  }

  // Impl
  override protected def doAction(state: ScaldingTask): Boolean = {
    // Get the source file
    val sourceFile = if(state.pathToScriptFile.isNullOrWhitespace) state.pathToJarFile else state.pathToScriptFile

    // Debug
    Log debug("Scalding Tool Configuration - Source: %s, ClassName: %s, DepJars: %s" format(sourceFile, state.jobClassName, state.dependencyJars.mkString(":")))

    // Create the tool
    val (tool, className, cleanupAction) = configureScalding(sourceFile, state.jobClassName, state.dependencyJars)

    // Create the arguments
    val args = Arguments(ExactSwitch(className), Switch("hdfs")) ++ state.arguments

    // Extract the config
    val config = tool.getConf

    // Info
    Log info("Executing Scalding Job - Arguments: '%s'" format args)

    // Ready to run
    val result = ToolRunner.run(config, tool, args.toShellArgs)

    // Cleanup
    cleanupAction()

    // result
    (result == 0)
  }

  override protected def doTest(state: ScaldingTask): Unit = {

    // Get the source file
    val sourceFile = if(state.pathToScriptFile.isNullOrWhitespace) state.pathToJarFile else state.pathToScriptFile

    // Create the tool
    val (tool, className, cleanupAction) = configureScalding(sourceFile, state.jobClassName, state.dependencyJars)

    // Create the arguments
    val args = Arguments(ExactSwitch(className), Switch("hdfs")) ++ state.arguments

    // Ready to run...

    // Cleanup
    cleanupAction()
  }


}
