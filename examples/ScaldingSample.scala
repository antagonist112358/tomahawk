package net.mentalarray.doozie.samples

import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.Tasks.ScaldingTask
import net.mentalarray.doozie.{Arguments, Param}

/**
 * Created by kdivincenzo on 11/5/14.
 */
class ScaldingSample extends WorkflowBuilder ("ScaldingDemo") {

  appendStep(ScaldingTask { task =>
    task.useCompiledJar("???")
    task.specifyArgs(
      Arguments(
        Param("logsDir", "/data/somefiles/etc..."),
        Param("output",  "/user/demo/SECSII_Output")
      )
    )
  })

}
