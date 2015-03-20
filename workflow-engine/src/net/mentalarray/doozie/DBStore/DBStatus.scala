package net.mentalarray.doozie.DBStore

/**
 * Created by bgilcrease on 9/10/14.
 */
object DBStatus{

  def exists(job: String, task: String) : Boolean = {
    val query: String = "select count(*) count from jobstatus js where js.job = ? and js.task = ?"
    val map =  JDBCConnection().HiveMetastore.fetchSingleRow(query, List((1, job),(2,task)))
    if ( map.isEmpty || map.get("count").toInt == 0) {
      false
    } else {
      true
    }
  }

  def getStatus(job: String, task: String): String = {
    val query: String = "select js.status status from jobstatus js where js.job = ? and js.task = ?"
    val map =  JDBCConnection().HiveMetastore.fetchSingleResult(query, List((1, job),(2,task)))
    map("status")
  }

  def setStatus(job: String, task: String, status: String) {
    if(exists(job, task)){
      val query: String = "update jobstatus set status = ?, rundate = current_timestamp where job = ? and task = ? "
      JDBCConnection().HiveMetastore.set(query, List((1,status),(2,job),(3,task)))
    } else {
      val query: String = "insert into jobstatus ( status, job, task, rundate) values ( ?, ?, ?, current_timestamp)"
      JDBCConnection().HiveMetastore.set(query, List((1,status),(2,job),(3,task)))
    }
  }

  def startTask(job: String, task: String) {
    setStatus(job,task,"Running")
  }

  def endTask(job: String, task: String, pass: Boolean) {
    val status = pass match {
      case true  => "Pass"
      case false => "Fail"
    }
    setStatus(job,task,status)
  }

}
