package net.mentalarray.doozie.Tasks

import java.util.Properties

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Created by kdivincenzo on 9/26/14.
 */
class PigTask(name: String) extends WorkflowTask(name) {
  // Task params
  private var _script: String = null
  private var _params: ReplacementParameters = null
  private val _jarPaths = mutable.ArrayBuffer.empty[String]
  private val _serverProps = new Properties

  // Properties
  def script: String = {
    if (_params != null)
      _params.formatText(_script)
    else
      _script
  }

  def jarPaths: Seq[String] = _jarPaths

  def serverProperties: Properties = _serverProps

  // Methods
  def registerJar(mode: RunMode, pathToJar: String) {
    mode match {
      case RunMode.HDFS => _jarPaths += Hadoop.toHdfsPath(pathToJar)
      case RunMode.LOCAL => _jarPaths += pathToJar
    }
  }

  def setScript(rawText: String) {
    _script = rawText
  }

  def setScriptReplacements(replacer: ReplacementParameters) {
    _params = replacer
  }

  def setPigServerProperty(key: String, value: String): Unit = {
    _serverProps.setProperty(key, value)
  }

  private def jarsToString = _jarPaths.mkString(",")
  private def jarsFromString(str : String) : Unit = str.split(",", -1).foreach { j => if (!j.isNullOrWhitespace) { _jarPaths += j } }

  private def propstoList = {
    for (key <- _serverProps.keys)
      yield (key, _serverProps.get(key))
  }

  // Xml serialization
  def toXml = {
<PigTask>
  <name>{name}</name>
  <script>{scala.xml.PCData(script)}</script>
  <jarPaths>{jarsToString}</jarPaths>
  <properties>{ for((key, value) <- propstoList)
      <property>
        <key>{key}</key>
        <value>{value}</value>
      </property>
    }</properties>
</PigTask>
  }

  // Task validation
  override def validate {
    // Make sure the script is set
    if (_script.isNullOrWhitespace) {
      throw new WorkflowStateException(this, "The pig script to execute must be specified.")
    }
  }

  def getDetails : String = {
    val sb = new mutable.StringBuilder()
    sb.append("Name: %s\n" format(name))
    .append("Script: %s\n" format(if (!_script.isNullOrWhitespace) "{Script Text}" else "{nothing}"))
    .append("Jars to Register:\n")
    _jarPaths.foreach(p => sb.append("\tJar Path: %s\n".format(p)))
    sb.append("Properties:\n")
    _serverProps.foreach(prop => sb.append(s"\tKey: ${prop._1}, Value: ${prop._2}\n"))
    sb.toString()
  }
}
  object PigTask {
    def apply(cfgFn: PigTask => Unit): PigTask = {
      val task = new PigTask("Execute Pig Job")
      cfgFn(task)
      task
    }
    def fromXml(node: scala.xml.Node) : PigTask = {
      // Create new task
      val pt = new PigTask((node \ "name").text)
      // Get the base64 encoded script text
      val scriptText = (node \ "script").text
      // Set the script
      pt.setScript(scriptText)
      // Set the jar paths
      pt.jarsFromString((node \ "jarPaths").text)
      // Read any properties
      (node \ "properties" \ "property").foreach { pNode =>
        pt.setPigServerProperty((pNode \ "key").text, (pNode \ "value").text)
      }

      // Return
      pt
    }
  }


