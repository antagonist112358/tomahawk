package net.mentalarray.doozie

import java.io.File

import com.twitter.util.Eval

/**
 * Created by bgilcrease on 9/25/14.
 */


case class WorkflowMode(get: String)

object WorkflowReader {

  private def file(path: String): File = {
    val file = new File(path)
    if ( !file.exists() ) {
      throw new Exception("File does not exist: "+path)
    }
    file
  }

  def scalaFile(path: String): WorkflowBuilder = {
    val evalCompiler = new Eval()
    evalCompiler[WorkflowBuilder](file(path))
  }

  def jarFile(path: String, className: String) = {
    ClassLoader.createInstanceFrom[WorkflowBuilder](className, path)
  }

  def apply(mode: WorkflowMode, filePath: String, classPath: Option[String] = None) = mode.get match {
    case "scala" => scalaFile(filePath)
    case "jar" => jarFile(filePath, classPath.get)
  }

}

