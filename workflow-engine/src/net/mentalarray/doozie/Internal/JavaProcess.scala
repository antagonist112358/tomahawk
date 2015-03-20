package net.mentalarray.doozie.Internal

import java.io.{File, InputStream}

import scala.concurrent.{Future, Promise}

object JavaProcess {

  private val classPath: String = Utility.Path.classPath
  private val javaHome: String = System.getProperty("java.home")
  private val javaBin: String = javaHome + File.separator + "bin" + File.separator + "java"
  private val Logger = new {} with Logging {}

  def runAndWait(entryPoint: Class[_], args : String*) : Int = runAndWait(null, entryPoint, args: _*)

  def runAndWait(properties : Seq[(String, String)], entryPoint: Class[_], args : String*): Int = {
    val className = entryPoint.getCanonicalName.replace("$", "")
    val propertyArgs = if (properties == null || properties.isEmpty)
      Array.empty[String]
    else
      properties.map( kv => {
        val (key, value) = kv
        "-D%s=%s".format(key,value) }
      ).toArray

    val javaArgs = Array("-cp", classPath) ++ propertyArgs ++ Array(className) ++ args
    val processArgs = Array(javaBin) ++ javaArgs
    val builder = new ProcessBuilder(processArgs: _*)

    Logger.Log debug s"JavaProcess Arguments: ${javaArgs.mkString(" ")}"


    val process = builder.start()

    process.waitFor()
    val errorCode = process.exitValue()

    if (errorCode != 0) {
      Logger.Log.error("JavaProcess - ErrorStream: " + streamToString(process.getErrorStream))
    }

    errorCode
  }

  def runAsync(entryPoint: Class[_], args : String*) : Future[Int] = runAsync(null, entryPoint, args: _*)

  def runAsync(properties : Seq[(String, String)], entryPoint: Class[_], args : String*): Future[Int] = {
    val className = entryPoint.getCanonicalName.replace("$", "")
    val propertyArgs = if (properties == null || properties.isEmpty)
      Array.empty[String]
    else
      properties.map( kv => {
        val (key, value) = kv
        "-D%s=%s".format(key,value) }
      ).toArray

    val javaArgs = Array("-cp", classPath) ++ propertyArgs ++ Array(className) ++ args
    val processArgs = Array(javaBin) ++ javaArgs
    val builder = new ProcessBuilder(processArgs: _*)

    Logger.Log debug s"JavaProcess Arguments: ${javaArgs.mkString(" ")}"

    val process = builder.start()
    val p = Promise[Int]()

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    Future {
      process.waitFor()
      process.exitValue() match {
        case 0 => p.success(0)
        case errNum: Int =>
          Logger.Log.error("JavaProcess - ErrorStream: " + streamToString(process.getErrorStream))
          p.failure(new RuntimeException(s"Process returned non-zero error code of $errNum"))
      }
    }

    p.future
  }

  private def streamToString(inStream : InputStream) : String = {
    org.apache.commons.io.IOUtils.toString(inStream)
  }
}

