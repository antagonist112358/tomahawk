package net.mentalarray.doozie.Configuration

import net.mentalarray.doozie.DBStore.{DBStatus, JDBCConnection}

class DBConfiguration(jobName: String, databaseConnection: JDBCConnection) extends Config(jobName) {

  private val _database: JDBCConnection = databaseConnection

  override protected def exists(task: String, key: String): Boolean = {
    val query: String = "SELECT count(*) " +
                        "FROM jobstatus js, " +
                             "jobparam jp " +
                        "WHERE js.job = ? " +
                          "AND js.task = ? " +
                          "AND jp.param = ? " +
                          "AND jp.workflowid = js.workflowid"
    val map =  _database.fetchSingleRow(query, List((1, job),(2,task),(3,key)))
    if ( map.isEmpty || map.get("count").toInt == 0) {
      false
    } else {
      true
    }
  }

  private def addParam(task: String, key: String, value: String): Unit = {
    if (exists(task, key)) {
      throw new UnsupportedOperationException("Element already exists key: " + key + " value: " + value + " \n For task: " + task)
    }
    val query: String = "insert into jobparam (workflowid, param, value) values((select workflowid from jobstatus where job =  ? and task = ?), ?, ?)"
    _database.set(query, List((1, job),(2,task),(3,key),(4,value)))

  }

  private def getParam(task: String, key: String): String = {
    val query: String = "Select jp.value from jobstatus js, jobparam jp where js.job = ? and js.task = ? and jp.param = ? and jp.workflowid = js.workflowid"
    val map =  _database.fetchSingleResult(query, List((1, job),(2,task),(3,key)))
    map("value")
  }

  private def setParam(task: String, key: String, value: String){
    if (exists(task, key)) {
      val query: String = "update jobparam set value = ?" +
        "where param = ? " +
        "and workflowid = (select workflowid from jobstatus " +
        "where job = ? " +
        "and task = ? )"
      _database.set(query, List((1, value), (2, key), (3, job), (4, task)))
    } else {
      addParam(task, key, value)
    }
  }

  def store(task: String, key: String, value: String) {
    if ( !DBStatus.exists(job, task) ) {
      DBStatus.setStatus(job,task,"Starting")
    }
    setParam(task, key, value)
  }

  def retrieve(task: String, key: String): String = {
    getParam(task, key)
  }

  def clearTaskParameters(task: String){
    val query: String = "delete from jobparam where workflowid = (select workflowid from jobstatus where job = ? and task = ?)"
    _database.set(query, List((1, job), (2, task)))
  }

}

object DBConfiguration{
  def apply(jobName: String, databaseConnection: JDBCConnection): DBConfiguration = {
    new DBConfiguration(jobName, databaseConnection)

  }

  def apply(jobName: String): DBConfiguration = {
    new DBConfiguration(jobName, JDBCConnection().HiveMetastore)
  }
}
