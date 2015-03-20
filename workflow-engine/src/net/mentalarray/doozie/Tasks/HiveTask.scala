package net.mentalarray.doozie.Tasks

/**
 * Created by bgilcrease on 10/1/14.
 */

class HiveTask(name:String) extends WorkflowTask(name) with TaskHasResult[String] with Logging {

  private var _statements: List[String] = null

  private def statementsOrEmpty = if (_statements == null) List.empty[String] else _statements

  override def validate {
    var isValid = true
    if ( _statements!=null ) {
      _statements.foreach(s => isValid &= !s.isNullOrWhitespace)
    } else {
      isValid = false
    }
    if ( !isValid ) {
      throw new WorkflowStateException(this, "The hive query must be specified.")
    }
  }

  def statements: List[String] = _statements

  def setNonQuery(statements: List[String]) = _statements = statements

  def setNonQuery(statement: String): Unit = setNonQuery(List(statement))

  def appendNonQuery(statements: List[String]) = _statements = statementsOrEmpty ++ statements

  def appendNonQuery(statement: String): Unit = appendNonQuery(List(statement))

}

object HiveTask {
  def apply(cfgFn: HiveTask => Unit): HiveTask = {
    val task = new HiveTask("Execute Hive Job")
    cfgFn(task)
    task
  }
  def apply(name: String)(cfgFn: HiveTask => Unit): HiveTask = {
    val task = new HiveTask(name)
    cfgFn(task)
    task
  }
}