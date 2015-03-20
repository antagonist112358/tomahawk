package net.mentalarray.doozie.DBStore

import java.sql.{DriverManager, ResultSet}

import scala.collection.mutable

/**
 * Created by bgilcrease on 9/10/14.
 */
trait SchemaBuilder extends JDBCConnection {
  implicit def tupleWhereCondition( wc: (String, String) ) = new WhereCondition(wc._1, wc._2)

  protected val selectColumn: String
  protected val table: String
  protected val whereConditions: List[WhereCondition]
  protected val orderByColumn: String

  protected case class WhereCondition(parameter: String, value: String)

  protected def query: String = {
    "select %s from %s where %s order by %s" format(selectColumn, table, whereClause, orderByColumn)
  }

  protected def whereClause: String = {
    whereConditions.foldLeft(List[String]()) { (res: List[String], a: WhereCondition) => res :+ "%s = %s".format(a.parameter, a.value)}.mkString(",")
  }

  //TODO implement typed fetch
  def schema(table: String, forceToString: Boolean = true): List[(String, String)] = {
    val DBConnection = getConnection
    var columnList: mutable.MutableList[(String, String)] = mutable.MutableList[(String, String)]()
    try {
      val statement = DBConnection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
      statement.setString(1, table)
      val result = statement.executeQuery()
      while (result.next()) {
        columnList = columnList :+(result.getString(1), "String")
      }
    } finally {
      DBConnection.close
    }
    columnList.toList
  }
}

trait DB2SchemaBuilder extends SchemaBuilder {

  override protected val selectColumn: String = "COLNAME"
  override protected val table: String = "syscat.columns"
  override protected val whereConditions: List[WhereCondition] = List(("tabname", "upper(?)"))
  override protected val orderByColumn: String = "COLNO"

}

protected[workflow] trait JDBCConnection {


  protected lazy val _username: String = _connectionInfo.user
  protected lazy val _password: String = _connectionInfo.password
  protected lazy val _connectionString: String = _connectionInfo.connectionString
  protected val _connectionInfo: DBConnectionString

  val fetchMap: (String, Option[List[(Int, String)]]) => Seq[Map[String, String]] = (query, params) => fetch(query, params)(mapResult)
  val fetchList: (String, Option[List[(Int, String)]]) => Seq[List[String]] = (query, params) => fetch(query, params)(listResult)
  val fetchSeq: (String, Option[List[(Int, String)]]) => Seq[Seq[String]] = (query, params) => fetch(query, params)(seqResult)

  protected def registerDriver: Unit

  private def useCredentials = {
    if (_username == null || _password == null) {
      false
    } else {
      true
    }
  }

  protected def getConnection = {
    if (useCredentials) DriverManager.getConnection(connectionString, _username, _password)
    else DriverManager.getConnection(connectionString)
  }

  def user = _username
  def password = _password

  def connectionString = {
    registerDriver
    _connectionString
  }

  def fetchSingleResult(query: String, params: List[(Int, String)]): Map[String, String] = {
    val option = fetchSingleRow(query, params)
    if (option.isEmpty) {
      throw new NoSuchElementException("No match for query " + query + " with params: \n" + params.mkString(","))
    }
    option.get
  }

  def fetchSingleRow(query: String, params: List[(Int, String)]): Option[Map[String, String]] = {
    val results = fetchMap(query, Option(params))
    if (results.isEmpty && results.count(x => !x.isEmpty) == 1) None
    else Option(results.head.toMap)
  }

  def fetchSingleRow(query: String): Option[Map[String, String]] = {
    val results = fetchMap(query, None)
    if (results.isEmpty && results.count(x => !x.isEmpty) == 1) None
    else Option(results.head.toMap)
  }

  def fetch[T](query: String, params: Option[List[(Int, String)]] = None)(fn: ResultSetFunction[ResultSet, T]): Seq[T] = {
    val DBConnection = getConnection
    try {
      val statement = DBConnection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)

      params match {
        case Some(xs) => xs.foreach(x => statement.setString(x._1, x._2))
        case None => None
      }

      val result = statement.executeQuery()
      val metaData = result.getMetaData()
      val colCount = metaData.getColumnCount()
      val resultIterator = new ResultSetIterator(result)
      resultIterator.foldLeft[Seq[T]](Seq[T]())((sp: Seq[T], x: ResultSet) =>
        sp ++ Seq[T](fn(x))
      )
    } finally {
      DBConnection.close
    }
  }

  def executeNonQuery(statements: List[String]): Boolean = {
    val DBConnection = getConnection
    try {
      val statement = DBConnection.createStatement()
      var status = true
      statements.foreach(nonQuery => status &= !statement.execute(nonQuery))
      status
    } finally {
      DBConnection.close
    }

  }

  def set(query: String, params: List[(Int, String)]) {
    val DBConnection = getConnection
    try {
      val statement = DBConnection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
      params.foreach(param => statement.setString(param._1, param._2))
      val result = statement.executeUpdate()
    } finally {
      DBConnection.close
    }
  }

  def insertBatch(table: String, data: Seq[Seq[String]]): Unit = {
    val DBConnection = getConnection
    try {
      DBConnection.setAutoCommit(false)
      val statement = DBConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
      data.grouped(5000).foreach( fn => {
        fn.foreach(row => statement.addBatch("INSERT INTO " + table + " VALUES( '" + row.mkString("','") + "' )"))
        statement.executeBatch
        DBConnection.commit
      })
      //    } catch {
      //      case BatchUpdateException => throw new Exception("BatchUpdateException caught with batch insert to %s " format table)
      //      case SQLException => throw new Exception("SQLException caught with batch insert to %s " format table)
      //      case _ => throw new Exception("Unhandled exception during batch insert")
    } finally {
      DBConnection.setAutoCommit(false)
      DBConnection.close
    }
  }
}

object JDBCConnection {
  def apply() = DatabaseLibrary
}

