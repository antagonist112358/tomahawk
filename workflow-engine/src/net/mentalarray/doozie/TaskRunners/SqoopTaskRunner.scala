package net.mentalarray.doozie.TaskRunners

import com.cloudera.sqoop.SqoopOptions

/**
 * Created by kdivincenzo on 9/9/14.
 */

class SqoopTaskRunner extends WorkflowTaskRunner with StateHelpers {

  type T = SqoopTask

  private def configureOptions(task: SqoopTask): SqoopOptions = {
    val options = new SqoopOptions()
    implicit val s: SqoopTask = task

    // Set required options
    options.setConnectString(task.connect)
    options.setTargetDir(task.targetDir)

    // Set mappers and fetch size
    options.setNumMappers(task.numMappers)
    options.setFetchSize(task.fetchSize)

    // Set either table or query
    if (!task.table.isNullOrWhitespace)
    {
      setFromState(j => j.table, options.setTableName)
      setFromStateIfPresent(j => j.where, options.setWhereClause)
    }
    else
    {
      setFromState(j => j.query, options.setSqlQuery)
    }

    // Set other variables
    setFromStateIfPresent(j => j.username, options.setUsername)
    setFromStateIfPresent(j => j.password, options.setPassword)
    setFromStateIfPresent(j => j.splitBy, options.setSplitByCol)


    options
  }

  // Impl
  override def doAction(state: SqoopTask): Boolean = {

    // Configure hadoop
    Application.hadoopConfiguration

    val options = configureOptions(state)

    // Create the import tool
    val importTool = new SqoopImportTool()

    // Validate the options through sqoop
    importTool.validate(options)

    // Run the tool
    val result = importTool.run(options)

    // Set success status
    (result == 0)
  }

  override def doTest(state: SqoopTask): Unit = {
    val options = configureOptions(state)

    // Create the import tool
    val importTool = new SqoopImportTool()

    // Validate the options through sqoop
    importTool.validate(options)
  }

}
