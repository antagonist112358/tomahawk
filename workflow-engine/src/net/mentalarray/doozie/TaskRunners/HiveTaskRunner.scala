package net.mentalarray.doozie.TaskRunners

/**
 * Created by bgilcrease on 10/1/14.
 */
class HiveTaskRunner  extends WorkflowTaskRunner with Logging {

  type T = HiveTask

  // Impl
  override protected def doAction(state: HiveTask): Boolean = {

    var status = true
    try {

      val hiveDB = ClassLoader.findClass[DatabaseLibrary](Application.settings.getSetting(Setting.HiveDB))

      Log debug("Hive command[s] which will be executed: %s" format(state.statements.mkString(";\n")))
      status = hiveDB.executeNonQuery(state.statements)
    } catch {
      case ex: Exception => {
        Log error("Error running Hive task for query: %s" format (state.statements), ex)
        throw ex
      }
    }
    status
  }

  override protected def doTest(state: HiveTask): Unit = {

    try {
      Log info "Validating task state..."
      Log info("Hive command[s] which would be executed: %s" format(state.statements.mkString(";\n")))
      state.validate
    } catch {
      case ex: Exception => {
        Log error("Error running Hive task for query: %s" format (state.statements), ex)
        throw ex
      }
    }
  }
}
