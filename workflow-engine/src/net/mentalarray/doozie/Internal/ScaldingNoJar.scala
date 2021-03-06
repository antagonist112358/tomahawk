package net.mentalarray.doozie.Internal

/**
 * Created by kdivincenzo on 10/2/14.
 * Stold from: https://github.com/ktoso/hadoop-scalding-nojartool
 */


import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

import com.twitter.scalding
import org.apache.hadoop
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.util.{Tool, ToolRunner}

import scala.collection.mutable.ListBuffer

/**
 * Allows running jobs in "real mode", instead of Cascading's "local mode" straight from the sbt console.
 *
 * It will bundle your local class files as dependencies, thus avoiding the usual "assemble the fat jar" process
 * before submitting a job. It is required still though to have all dependencies available to Hadoop somehow - you can either
 * use the `libJars` parameter, or put your dependencies on the nodes using some other method.

 * Implements the same functionality as Hadoop's [[org.apache.hadoop.util.RunJar]] which is invoked when
 * you use `hadoop jar my-scalding-job.jar`, but does not require building the jar file beforehand.
 *
 * <br/>
 * <br/>
 *
 * '''Note on why this class is needed''': If you'd try to run a Cascading job with
 * `ToolRunner.run(conf, new scalding.Tool, args)`, it will properly detect and use the `Hdfs` "mode",
 * however Cascading will STILL select the `org.apache.hadoop.mapred.LocalJobRunner`,
 * and ''WON'T'' submit the task to the Cluster!
 *
 * @param collectClassesFrom should point at `target/scala-x.xx/target` if you're in an sbt build.
 * @param libJars local *.jar or *.class files which will be distributed to all nodes executing the job.
 *                Preferably distribute your scalding/cascading (and other libs) using this param.
 *                See also: [[http://grepalex.com/2013/02/25/hadoop-libjars/ hadoop-libjars]].
 *
 * @author Konrad 'ktoso' Malawski <konrad.malawski@project13.pl>
 */

protected[workflow] object ScaldingNoJar {

  def buildTool(libJars: List[File]) = {
    new NoJarTool(new scalding.Tool, None, libJars)
  }

}

protected[workflow] class NoJarTool(
                 wrappedTool: hadoop.util.Tool,
                 collectClassesFrom: Option[File] = Some(new File("target/scala-2.10/classes/")),
                 libJars: List[File] = Nil
                 ) extends Tool {

  //private var config = new Configuration()
  private var config = Application.hadoopConfiguration

  protected def run(args: Array[String]): Int = {
    checkIfConfigValidForRealMode(config)

    val jars = libJars.map(jar => prefixWithFileIfNeeded(jar.toString))

    collectClassesFrom match {
      case Some(classesDir) => {
        val classes = collectClasses(classesDir) map { clazz => prefixWithFileIfNeeded(clazz.toFile.getAbsolutePath) }
        setLibJars(config, classes ++ jars)
      }
      case None => {
        setLibJars(config, jars)
      }
    }

    /*
    collectClassesFrom map { classesDir =>
      val classes = collectClasses(classesDir) map { clazz => prefixWithFileIfNeeded(clazz.toFile.getAbsolutePath) }
      val jars = libJars.map(jar => prefixWithFileIfNeeded(jar.toString))

      setLibJars(config, classes ++ jars)
    }
    */

    ToolRunner.run(config, wrappedTool, args)
  }

  protected def collectClasses(classesDir: File): List[Path] = {
    val buffer = new ListBuffer[Path]() // paths to include
    val base = classesDir.toPath

    Files.walkFileTree(base, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.toFile.isFile)
          buffer += file

        FileVisitResult.CONTINUE
      }
    })

    buffer.toList
  }

  /**
   * Check's if the user didn't accidentaly break our config into Local mode.
   * See Cascading's [[cascading.flow.hadoop.HadoopFlowStep]] to see how Cascading decides if it should use
   * [[org.apache.hadoop.mapred.LocalJobRunner]] or the "real" one.
   */
  def checkIfConfigValidForRealMode(conf: Configuration) {
    val key: String = "mapred.job.tracker"
    val jobTracker = conf.get(key)

    if(jobTracker == "local") {
      // todo, proper logging?
      println(s"[WARNING] Expected [$key] to be set to something else than 'local', since this forces Cascading to use LocalJobRunner! " +
        s"This is probably not what you wanted if you're using ${getClass.getSimpleName}.")
    }
  }

  /**
   * This is effectively the same as passing "-libjars" in the command line for `hadoop`.
   * Internally the bellow key is used for them though, see [[org.apache.hadoop.util.GenericOptionsParser]], for details.
   *
   * @param config config to be updated
   * @param jarsOrClasses '''local''' paths to dependencies, such as class files of your Job, or Scalding's jar itself.
   */
  def setLibJars(config: Configuration, jarsOrClasses: List[String]) {
    config.setStrings("tmpjars", jarsOrClasses: _*)
  }

  override def setConf(conf: Configuration): Unit = this.config = conf

  override def getConf: Configuration = config

  private def prefixWithFileIfNeeded(path: String): String = {
    val prefix = "file:///"

    if (path.startsWith(prefix)) path
    else prefix + path
  }
}
