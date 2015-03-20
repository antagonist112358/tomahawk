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

package net.mentalarray.doozie

import java.io.File

import net.mentalarray.doozie.Utility.{Hadoop, Path}
import org.apache.hadoop.conf.{Configuration => HadoopConfiguration}
import org.apache.hadoop.fs.{Path => HadoopPath}

import scala.collection.mutable

/**
 * Represents the two different run modes of DataFlow or Map/Reduce for that matter:
 * LOCAL mode, and HDFS mode.
 * @define modeName Textual representation of the run mode.
 */

sealed trait RunMode { def modeName: String }
object RunMode {

  /**
   * Local (non-cluster) mode
   */
  case object LOCAL extends RunMode { val modeName = "Local" }

  /**
   * HDFS (distributed) mode
   */
  case object HDFS  extends RunMode { val modeName = "HDFS" }
}

/**
 * Base trait which all Settings derive from.
 * @define name The setting namespace and name.
 * @define defautlValue The default value of the setting.
 */
sealed trait Setting { def name: String; def defaultValue: String }

/**
 * Setting object that contains all settings.
 * Only settings which have been predefined here with default values will be read in from the settings file.
 */
object Setting {
  private lazy val _values = SealedTraitIterator.values[Setting].toSeq

//  private lazy val _values = Seq(HDFSNameNode)

  case object HDFSNameNode extends Setting {
    val name = "dataflow.hdfs.namenode"
    val defaultValue = "???"
  }

  case object HDFSBin extends Setting {
    val name = "dataflow.hdfs.bin"
    val defaultValue = "/usr/bin"
  }

  case object HDFSCoreSite extends Setting {
    val name = "dataflow.hdfs.core-site.path"
    val defaultValue = "/etc/gphd/hadoop/conf/core-site.xml"
  }

  case object HDFSSite extends Setting {
    val name = "dataflow.hdfs.hdfs-site.path"
    val defaultValue = "/etc/gphd/hadoop/conf/hdfs-site.xml"
  }

  case object Threadpool extends Setting {
    val name = "dataflow.threadpool.size"
    val defaultValue = 5.toString
  }

  case object HiveDB extends Setting {
    val name = "dataflow.hivedb.classpath"
    val defaultValue = "net.mentalarray.doozie.DatabaseLibrary.DatabaseLibrary.HiveDB"
  }

  def values = _values

}

/**
 * Base class for Settings repository.
 */
abstract class Settings {
  /**
   * Gets a Setting value using the Setting case object.
   * @param setting The setting object to retrieve the value for.
   * @return The string value of the setting (or the default value if no value was loaded).
   */
  def getSetting(setting: Setting) : String
}

/**
 * Base trait for Temporary File / Temporary Directory management.
 */
trait TempManager {

  /**
   * Sets up a new Randomly generated Temporary Directory path.
   * @note This should only be used once per execution, as it sets the Java global 'java.io.tempdir'
   *       environment variable.
   * @return The full path of the temporary directory.
   */
  def setTemporaryDirectory : String

  /**
   * Clears the temporary directory, deletes the temporary directory, then recreates it.
   * @return The full path of the temporary directory.
   */
  def clearTemporaryDirectory : String

  /**
   * Deletes the temporary directory and all of its contents.
   */
  def deleteTemporaryDirectory : Unit

  /**
   * Prevents the temporary directory from being cleared
   */
  def lockTemporaryDirectory : Unit

  /**
   * Allows the temporary directory to be cleared (after a lock).
   */
  def unlockTemporaryDirectory : Unit

  def get : String

}

/**
 * The Application represents the running Application instance or the process currently running.
 * It does not represent a Shell application or a daemon specifically, but rather a particular instance of
 * an application currently executing (as a daemon, in the shell, etc.)
 * As such it contains Singletons for application level items, such as:
 * Settings,
 * TempDirectory Management,
 * Current Directory,
 * Application Title and Version,
 * Hadoop Configuration,
 * etc.
 */
object Application {

  // Settings implementation class
  protected [Application] class SettingsImpl(settingsPath: String) extends Settings {

    private val lockerObject = new Object()

    private def buildSettings(): mutable.Map[String, String] = {

      val importedSettings = Utility.Path.getPropXmlAsMap(settingsPath)
      val settings = Setting.values
      val results = mutable.Map[String,String]()

      settings.map( f => results += (f.name -> importedSettings.getOrElse(f.name, f.defaultValue)))
      results
    }

    // Add all the default values
    private lazy val settingsMap = buildSettings

    // Load the settings here

    override def getSetting(setting: Setting) : String = {
      lockerObject.synchronized {
        settingsMap(setting.name)
      }
    }
  }

  protected [Application] class TempManagerImpl extends TempManager with Logging {

    // This is the single (per execution) uniqueID which will become M/R temp-space
    private final val _tmpPath = Utility.Path.combine("/tmp", java.util.UUID.randomUUID().toString)

    // Used to control the lock status
    private var _isLocked = false

    override def setTemporaryDirectory: String = {
      // Create the temporary path
      new File(_tmpPath).mkdir

      // Set the tempPath for this job
      System.setProperty("java.io.tmpdir", _tmpPath)

      // Return the path
      _tmpPath
    }

    override def get: String = _tmpPath

    override def clearTemporaryDirectory: String = {
      val tmpPath = new File(_tmpPath)
      if (!_isLocked) {
        delete(tmpPath)
        tmpPath.mkdir
      } else
        Log info "Temporary directory can not be cleared while it is locked."

      _tmpPath
    }

    override def deleteTemporaryDirectory: Unit = {
      delete(new File(_tmpPath))
    }

    override def lockTemporaryDirectory : Unit = _isLocked = true

    override def unlockTemporaryDirectory : Unit = _isLocked = false

    // Define recursive deletion method
    private def delete(file: File): Boolean = {
      if (file.isDirectory) {
        Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(delete(_))
      }
      file.delete
    }

  }

  private lazy val _frameworkDirectory = {
    val jarFilePath = new java.io.File(Utility.Path.combine(Application.getClass
      .getProtectionDomain.getCodeSource.getLocation.toURI.getPath, "../"))
    jarFilePath.getCanonicalPath
  }

  private lazy val _applicationDirectory = System.getProperty("app.currentDirectory")

  private lazy val _currentDirectory = new java.io.File(".").getCanonicalPath

  private val SettingsFilename = "dataflow" + ".xml"

  // Settings singleton
  private val _instance = new SettingsImpl(Path.combine(Application.applicationDirectory+"/conf", SettingsFilename))

  // TempManager singleton
  private val _tmpMgrInstance = new TempManagerImpl

  // Hadoop config stuff
  HadoopConfiguration.addDefaultResource(Application.settings.getSetting(Setting.HDFSCoreSite))

  // Used to force instantiation of this object (static constructor)
  def boot : Unit = { }

  // Get the application settings
  def settings : Settings = _instance

  def temporaryManager : TempManager = _tmpMgrInstance

  /**
   * Gets the Application's Title
   * @return The title of the currently executing application package.
   */
  def title = getClass.getPackage.getImplementationTitle

  /**
   * Gets the Application's Version
   * @return The version of the currently executing application package.
   */  
  def version = getClass.getPackage.getImplementationVersion

  /**
   * @return The current directory (probably where the application was invoked from).
   */
  def currentDirectory = _currentDirectory

  /**
   * @return The directory which contains the jar-file which is currently executing.
   */
  def frameworkDirectory = _frameworkDirectory

  /**
   * @return The root application directory for dataflow (wherever it is installed)
   */
  def applicationDirectory = _applicationDirectory

  /**
   * Returns a new configured instance of the HadoopConfiguration class.
   * @return A HadoopConfiguration class instance, which already has the 'core-site.xml',
   *         'hdfs-site.xml', and 'mapred-site.xml' added into it.
   */
  def hadoopConfiguration = {
    val hadoopConf = new HadoopConfiguration()

    hadoopConf.addResource(new HadoopPath(Hadoop.coreSiteFile))
    hadoopConf.addResource(new HadoopPath(Hadoop.hdfsSiteFile))
    hadoopConf.addResource(new HadoopPath(Hadoop.mapredSiteFile))

    hadoopConf
  }


}

