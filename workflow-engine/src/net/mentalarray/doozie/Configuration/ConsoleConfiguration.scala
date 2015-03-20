package net.mentalarray.doozie.Configuration

import scala.collection.mutable

/**
 * Created by bgilcrease on 12/30/14.
 */
class ConsoleConfiguration(jobName: String) extends Config(jobName) {
  private var _variables = mutable.Map.empty[String, VariablesMap]

  override protected def exists(task: String, key: String): Boolean ={
    if (_variables.get(task).isEmpty){
      _variables = mutable.Map[String, VariablesMap]()
      false
    } else {
      if ( _variables.get(task).isEmpty ) {
        false
      } else {
        true
      }
    }
  }

  override def retrieve(task: String, key: String): String = {
    _variables(task).retrieve(key)
  }

  override def store(task: String, key: String, value: String): Unit = {
    _variables(task).store(key, value)
  }

  def clearTaskParameters(task: String): Unit ={
  }

}


