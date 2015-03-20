package net.mentalarray.doozie.Tasks

/**
 * Created by bgilcrease on 11/4/14.
 */
class SparkTask(name: String) extends WorkflowTask(name) with Logging {

  private val optMap = collection.mutable.Map[String, String]().withDefaultValue("")

  override def validate: Unit = {
    if (jar.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Jar file for Spark job must be provided in SparkTask")

    if (classPath.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Class in jar file for Spark job must be provided in SparkTask")
  }

  def jar: String = { optMap("jar") }

  def classPath: String = { optMap("class") }

  def arg: String = { optMap("arg") }

  def amClass: String = { optMap("am-class") }

  def driverMemory: String = { optMap("driver-memory") }

  def numExecutors: String = { optMap("num-executors") }

  def executorMemory: String = { optMap("executor-memory") }

  def exectorCores: String = { optMap("executor-cores") }

  def queue: String = { optMap("queue") }

  def jobName: String = { optMap("name") }

  def addJars: String = { optMap("addJars") }

  def files: String = { optMap("files") }

  def archives: String = { optMap("archives") }

  def getArrayOfArgs: Array[String] = {
    val results = collection.mutable.MutableList[String]()
    optMap.toSeq.map( t =>  { results += "--"+t._1 ; results += t._2 } )
    results.toArray
  }

  def jar(value: String): SparkTask = { optMap.addOrReplace("jar",value); this }

  def classPath(value: String): SparkTask = { optMap.addOrReplace("class",value); this }

  def arg(value: String): SparkTask = { optMap.addOrReplace("arg",value); this }

  def amClass(value: String): SparkTask = { optMap.addOrReplace("am-class",value); this }

  def driverMemory(value: String): SparkTask = { optMap.addOrReplace("driver-memory",value); this }

  def numExecutors(value: String): SparkTask = { optMap.addOrReplace("num-executors",value); this }

  def executorMemory(value: String): SparkTask = { optMap.addOrReplace("executor-memory",value); this }

  def exectorCores(value: String): SparkTask = { optMap.addOrReplace("executor-cores",value); this }

  def queue(value: String): SparkTask = { optMap.addOrReplace("queue",value); this }

  def jobName(value: String): SparkTask = { optMap.addOrReplace("name",value); this }

  def addJars(value: String): SparkTask = { optMap.addOrReplace("addJars",value); this }

  def files(value: String): SparkTask = { optMap.addOrReplace("files",value); this }

  def archives(value: String): SparkTask = { optMap.addOrReplace("archives",value); this }

}

object SparkTask {
  def apply(cfgFn: SparkTask => Unit): SparkTask = {
    val task = new SparkTask("SparkJob")
    cfgFn(task)
    task
  }
}
