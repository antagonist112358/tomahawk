package net.mentalarray.doozie.Configuration

/**
 * Created by bgilcrease on 9/8/14.
 */

abstract class Config(jobName:  String) {
  val job: String = jobName
  protected def exists(task: String, key: String): Boolean
  def retrieve(task: String, key: String): String

  def retrieve(task: String, key: String, defaultValue: String): String = {
    if ( exists (task, key)) {
      retrieve(task, key)
    } else {
      store(task,key,defaultValue)
      defaultValue
    }
  }

  def store(task: String, key: String, value: String)
  def clearTaskParameters(task: String)
}

object Config{

  private var _jobConfig: Config= null
  private var _jobName: String = null

  private def checkInstance() {
    if ( _jobName == null ) { throw new Exception("Configuration not initialized, please provide Config Type and workflow name")}
    if ( _jobConfig == null) {throw new Exception("Configuration not initialized, please set Config Type and workflow name")}
  }

  def clearTaskParameters(task: String): Unit ={
    checkInstance()
    _jobConfig.clearTaskParameters(task)
  }

  def setDBConfiguration(name: String) {
    _jobName = name
    _jobConfig = DBConfiguration(_jobName)
  }

  def setConsoleConfiguration(name: String) {
    _jobName = name
    _jobConfig = new ConsoleConfiguration(_jobName)
  }

  def apply[T: StringConverter](taskName: String, key: String): T = {
    checkInstance()
    val converter = implicitly[StringConverter[T]]
    converter convert _jobConfig.retrieve(taskName, key)
  }

  def apply[T: StringConverter](taskName: String, key: String, defaultValue: String): T = {
    checkInstance()
    val converter = implicitly[StringConverter[T]]
    converter convert _jobConfig.retrieve(taskName, key, defaultValue)
  }

  def update(taskName: String, key: String, value: AnyVal) {
    checkInstance()
    _jobConfig.store(taskName, key, value.toString )
  }

  def update(taskName: String, kv: List[(String, AnyVal)]) {
    checkInstance()
    kv.foreach( pair => _jobConfig.store(taskName, pair._1, pair._2.toString) )
  }

  def update(taskName: String, key: String, value: String) {
    checkInstance()
    _jobConfig.store(taskName, key, value )
  }

}

