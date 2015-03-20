package net.mentalarray.doozie.TaskRunners

import java.util.Properties

import net.mentalarray.doozie.PigSupport.PigServer
import net.mentalarray.doozie.Tasks.PigTask
import net.mentalarray.doozie.{Application, Logging, Utility => Util, WorkflowTaskRunner}
import org.apache.pig.ExecType
import org.apache.pig.backend.hadoop.datastorage.ConfigurationUtil
import org.apache.pig.impl.PigContext
import org.apache.pig.impl.util.PropertiesUtil
import org.apache.pig.scripting.Pig

import scala.concurrent._
import scala.concurrent.duration._

/*
 * Created by kdivincenzo on 9/26/14.
 */

// Note - Currently this runner expects the user to have specified the register commands for various jar files and UDFs
// within the script itself. Need to test this feature (added, but no verification)
// Todo - Extend PigTask to allow users to register Jar files and UDFs for all Pig Scripts run within a workflow.

class PigTaskRunner extends WorkflowTaskRunner with Logging {

  type T = PigTask

  private def buildPigJob(taskDef: PigTask) = {
    // Register the Jars
    for (jar <- taskDef.jarPaths) {
      Pig.registerJar(jar)
    }

    // Create the configuration
    val config = Application.hadoopConfiguration

    // Setup properties
    val props = new Properties
    PropertiesUtil.loadDefaultProperties(props)
    props.putAll(taskDef.serverProperties)
    props.putAll(ConfigurationUtil.toProperties(config))

    // Create a pigContext
    val pigContext = new PigContext(ExecType.MAPREDUCE, props)

    // Grunt really loud!
    val eggsAndBacon = new PigInBlanket(taskDef.script, pigContext)

    // Return the delicious food
    eggsAndBacon
  }

  // Impl
  override protected def doAction(state: PigTask): Boolean = {

    // Serialize the state
    //val xmlState = state.toXml.toString
    //Log debug s"PigTask XML:\n$xmlState"

    try {

      Log debug "Assigning PigTask to PigServer service..."

      val stubOutcome = PigServer.assignWork(state)

      Log debug "Sent PigTask to PigServer service, awaiting outcome..."

      // Wait for the result, then attempt to interpret it
      val outcome = Await.result(stubOutcome, Duration.Inf)

      // Return the result
      outcome

    } catch {
      case e: Exception =>
        Log error("Encountered an error while trying to execute PigTask.", e)
        false
    }

  }

  override protected def doTest(state: PigTask): Unit = {
    // Create the pigServer
    val pigRunner = buildPigJob(state)

    try {

      pigRunner.checkScript()

    } finally {
      if (pigRunner != null) {
        pigRunner.shutdown()
      }
    }

  }

}
