package net.mentalarray.doozie.Tasks

import java.io.File

import scala.collection.mutable

/**
 * Created by kdivincenzo on 10/2/14.
 */
class ScaldingTask(name: String) extends WorkflowTask(name) {

  private var _pathToScript: String = null
  private var _pathToJar: String = null
  private var _arguments: Arguments = null
  private var _jobClassName: String = null

  private val _dependencyJars = mutable.MutableList.empty[String]


  def pathToScriptFile: String = _pathToScript
  def pathToJarFile: String = _pathToJar
  def dependencyJars: Seq[String] = _dependencyJars
  def arguments: Arguments = _arguments
  def jobClassName: String = _jobClassName

  // These methods specify the scalding job
  def useScaldingScript(path: String) = _pathToScript = path

  def useCompiledJar(path: String, className: String) = { _pathToJar = path; _jobClassName = className }

  def addDependency(file: String) = _dependencyJars += file

  def addDependencies(files: String*) = files.foreach(_dependencyJars += _)

  def specifyArgs(args: Arguments) = _arguments = args
  
  override def validate: Unit = {
    val scriptSet = !_pathToScript.isNullOrWhitespace
    val jarSet = !_pathToJar.isNullOrWhitespace

    // Make sure at least one of the above two is set (but not more than 1)
    if (List(scriptSet, jarSet).map(set => if (set == true) 1 else 0).sum != 1) {
      throw new WorkflowStateException(this, "Exactly one of the three use... methods can be used to specify the input Scalding job.\n" +
        s"Actual inputs were: ScaldingScript: $pathToScriptFile, CompiledJar: $pathToJarFile")
    }

    // Check to make sure that all the dependencies can be found in the classPath
    dependencyJars.foreach(fileName => {
      val file = new File(fileName)
      if (!file.exists() && Path.resolveJarInClassPath(fileName) == None) {
        throw new WorkflowStateException(this, s"Cannot resolve scaldingTask dependency: $fileName")
      }
    })
  }

}

object ScaldingTask {
  def apply(fn: ScaldingTask => Unit): ScaldingTask = {
    val state = new ScaldingTask("ScaldingTask")
    fn(state)
    state
  }
}