package net.mentalarray.doozie.DBStore

import java.sql.ResultSet

import scala.collection.mutable

/**
 * Created by bgilcrease on 10/16/14.
 */

trait ResultSetFunction[T <: ResultSet, R] extends AnyRef {
  def apply(v1: T): R
}

object ResultSetFunction {

  val seqResult: ResultSetFunction[ResultSet, Seq[String]] = (rs: ResultSet) => {
    val metaData = rs.getMetaData()
    val colCount = metaData.getColumnCount()
    List.range(1, colCount + 1).foldLeft(Seq[String]()) { (seq, value) =>
      val colLabel: String = metaData.getColumnLabel(value)
      val colValue: String = rs.getString(colLabel)
      colValue +: seq
    }.toSeq.reverse
  }
  val mapResult: ResultSetFunction[ResultSet, Map[String, String]] = (rs: ResultSet) => {
    val metaData = rs.getMetaData()
    val colCount = metaData.getColumnCount()
    List.range(1, colCount + 1).foldLeft(mutable.Map[String, String]()) { (map, value) =>
      val colLabel: String = metaData.getColumnLabel(value)
      val colValue: String = rs.getString(colLabel)
      map(colLabel) = colValue
      map
    }.toMap
  }
  val listResult: ResultSetFunction[ResultSet, List[String]] = (rs: ResultSet) => {
    val metaData = rs.getMetaData()
    val colCount = metaData.getColumnCount()
    List.range(1, colCount + 1).foldLeft(List[String]()) { (list, value) =>
      val colValue: String = rs.getString(value)
      colValue +: list
    }.toList.reverse
  }

  implicit def resultFunction[T <: ResultSet, R](fn: T => R): ResultSetFunction[T, R] = new ResultSetFunction[T, R] {
    def apply(v1: T): R = fn(v1)
  }
}

object ResultSetIteratorImplicits {
  implicit def ResultToIterator(result: ResultSet): ResultSetIterator = {
    new ResultSetIterator(result)
  }
}


class ResultSetIterator(result: ResultSet) extends Iterator[ResultSet] {

  override def hasNext: Boolean = !result.isClosed && !result.isLast && !result.isAfterLast

  override def next(): ResultSet = {
    result.next()
    result
  }

}

