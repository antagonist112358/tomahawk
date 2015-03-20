import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.DSL.Extensions._
import net.mentalarray.doozie.DSL.{Replace, Tasks, TextFile, _}
import net.mentalarray.doozie.Logging
import net.mentalarray.doozie.Tasks._

/**
 * Created by bwilson on 11/4/14.
 */

class WaferETPig extends WorkflowBuilder ("WaferETPig") with Logging {

  private val jarLibraryList: String =
    """REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/Unstack.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/PiggyBank.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/datafu-1.2.0.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-pig-4.5.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-pig-4.5-sources.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-core-4.5-sources.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-core-4.5.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-hadoop-compat-4.5.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-rcfile-4.5.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/joda-time-1.6.2.jar';
    REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/elephant-bird-hive-4.5.jar';
    REGISTER '/usr/lib/gphd/hive/lib/hive-exec-0.12.0-gphd-3.0.1.0.jar';
    REGISTER '/usr/lib/gphd/hive/lib/hive-common-0.12.0-gphd-3.0.1.0.jar';"""

  /** Get the list of directories and create the parsing list for each of the processid's that will be looped through **/

  private val parsingList = getResultFrom(HdfsTask(task => {
    task.setCommand("ls /data/raw/etwafer/")
  }))

  val lcParsingList = deferredResult { reader =>
    reader extract(parsingList) toLowerCase
  }

  final val regex = "etwafer\\/process_id=(.*)".r

  private val seqlist = parsingList.mutate {
    _.split('\n')
      .toList
      .filter(s => s.contains("etwafer"))
      .map(v => regex.findFirstMatchIn(v).get.group(1))
      .filterNot(u => u.startsWith("-"))
      .filterNot(w => w.startsWith("K"))
  }

  /*

  How-to: Use deferredResult / TaskResult inside of a new Task

  appendStep(
    taskWithResults { reader =>
      HdfsTask(task => {

      })
    }
  )

  */

  /** Define standard locations for the job to store and process the job **/

  final val tmpDirectory = "/tmp/Pig/WaferET"
  final val hostDirectory = "/data/hosted/WaferET"
  final val localfsStaging = "/hadoop/hdtmp/WaferET"

  /** SED header and schema cleaning command **/

  final val headerSchemaCleanSED = "sed -i 1s/\\w\\+:://g"


  /** Cleanup the old directories from the unstack functions and the temporary hosting directories **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val hostedfileCleanup = Tasks(
      s"rm -r -f $tmpDirectory/WaferET$i".hdfsCmd,
      s"rm -r -f $tmpDirectory/itemUnstack$i".hdfsCmd,
      s"rm -r -f $tmpDirectory/stepSeqUnstack$i".hdfsCmd,
      s"rm -r -f $tmpDirectory/StackedWaferET$i".hdfsCmd
    )
    hostedfileCleanup.ignoreError = true
    hostedfileCleanup
  }))

  /** Execute the pig script **/

  appendStep(ForeachTask(seqlist, { i: String =>
    PigTask{task => task.setScript(TextFile.loadFromHdfs("%s%s.pig" format("/apps/scripts/", "WaferET")))
      task.setScriptReplacements(Replace(
        "processID" -> i,
        "daysBack" -> 365,
        "loadDirectory" -> "/data/raw/etwafer/process_id=",
        "itemFilter" -> "AVG",
        "tmpDirectory" -> tmpDirectory,
        "jarLibrary" -> jarLibraryList
      ))
      Log.debug(task.script)}
  }))

  /** Clean up the user hosted files **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val hostedfileCleanup = Tasks(
      s"rm -r -f $hostDirectory/WaferET$i.csv".hdfsCmd,
      s"rm -r -f $hostDirectory/WaferET$i".hdfsCmd,
      s"rm -r -f $hostDirectory/StackedWaferET$i".hdfsCmd,
      s"rm -r -f $hostDirectory/StackedWaferET$i.csv".hdfsCmd
    )
    hostedfileCleanup.ignoreError = true
    hostedfileCleanup
  }))

  /** copy dataz from temporary directories to the staging directories **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val copyDirectories = Tasks (
      s"cp $tmpDirectory/WaferET$i $hostDirectory/WaferET$i".hdfsCmd,
      s"cp $tmpDirectory/StackedWaferET$i $hostDirectory/StackedWaferET$i".hdfsCmd
    )
    copyDirectories
  }))

  /** clean out the header and schema files from the staged directory **/

  appendStep (ForeachTask(seqlist, {i: String =>
    val headerSchemaClean = Tasks (
      s"rm -r -f $hostDirectory/WaferET$i/.pig_header".hdfsCmd,
      s"rm -r -f $hostDirectory/WaferET$i/.pig_schema".hdfsCmd,
      s"rm -r -f $hostDirectory/StackedWaferET$i/.pig_header".hdfsCmd,
      s"rm -r -f $hostDirectory/StackedWaferET$i/.pig_schema".hdfsCmd
    )
    headerSchemaClean
  }))

  /** Create the local file system directories **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val localfscreate = Tasks (
      s"mkdir $localfsStaging/WaferET$i".shellCmd,
      s"mkdir $localfsStaging/StackedWaferET$i".shellCmd
    )
    localfscreate
  }))

  /** Copy over the header and schema files to the local fs **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val headerschemacopy = Tasks (
      s"copyToLocal $tmpDirectory/WaferET$i/.pig_header $localfsStaging/WaferET$i/.pig_header".hdfsCmd,
      s"copyToLocal $tmpDirectory/WaferET$i/.pig_schema $localfsStaging/WaferET$i/.pig_schema".hdfsCmd,
      s"copyToLocal $tmpDirectory/StackedWaferET$i/.pig_header $localfsStaging/StackedWaferET$i/.pig_header".hdfsCmd,
      s"copyToLocal $tmpDirectory/StackedWaferET$i/.pig_schema $localfsStaging/StackedWaferET$i/.pig_schema".hdfsCmd
    )
    headerschemacopy
  }))

  // use sed to strip out the stuff we don't want from those header and schema files.

  appendStep(ForeachTask(seqlist, {i: String =>

    val sedScript = Tasks(
      s"$headerSchemaCleanSED $localfsStaging/WaferET$i/.pig_header".shellCmd,
      s"$headerSchemaCleanSED $localfsStaging/WaferET$i/.pig_schema".shellCmd,
      s"$headerSchemaCleanSED $localfsStaging/StackedWaferET$i/.pig_header".shellCmd,
      s"$headerSchemaCleanSED $localfsStaging/StackedWaferET$i/.pig_schema".shellCmd
    )
    sedScript
  }))

  /** Copy the header files back over to the hosted directory. **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val headerRecovery = Tasks(
      s"copyFromLocal $localfsStaging/WaferET$i/.pig_header $hostDirectory/WaferET$i/.pig_header".hdfsCmd,
      s"copyFromLocal $localfsStaging/StackedWaferET$i/.pig_header $hostDirectory/StackedWaferET$i/.pig_header".hdfsCmd
    )
    headerRecovery
  }))

  /** Perform a -getmerge on the hostDirectories and save the .csv file to the local fs **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val filemerge = Tasks(
      s"getmerge $hostDirectory/WaferET$i/ $localfsStaging/WaferET$i.csv".hdfsCmd,
      s"getmerge $hostDirectory/StackedWaferET$i/ $localfsStaging/StackedWaferET$i.csv".hdfsCmd
    )
    filemerge
  }))

  /** Move the .csv back to hdfs **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val csvMove = Tasks(
      s"copyFromLocal $localfsStaging/WaferET$i.csv $hostDirectory/WaferET$i.csv".hdfsCmd,
      s"copyFromLocal $localfsStaging/StackedWaferET$i.csv $hostDirectory/StackedWaferET$i.csv".hdfsCmd
    )
    csvMove
  }))

  /** Move the schema files back to the hosted directory **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val schemaRecovery = Tasks(
      s"copyFromLocal $localfsStaging/WaferET$i/.pig_schema $hostDirectory/WaferET$i/.pig_schema".hdfsCmd,
      s"copyFromLocal $localfsStaging/StackedWaferET$i/.pig_schema $hostDirectory/StackedWaferET$i/.pig_schema".hdfsCmd
    )
    schemaRecovery
  }))

  /** cleanup the local file system directories **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val localClean = Tasks(
      s"rm -r -f $localfsStaging/WaferET$i".shellCmd,
      s"rm -r -f $localfsStaging/WaferET$i.csv".shellCmd,
      s"rm -r -f $localfsStaging/StackedWaferET$i".shellCmd,
      s"rm -r -f $localfsStaging/StackedWaferET$i.csv".shellCmd
    )
    localClean
  }))

  /** Get rid of the crc files from copy verification. **/

  appendStep(ForeachTask(seqlist, {i: String =>
    val crcClean = Tasks(
      s"rm -r -f $localfsStaging/.WaferET$i.csv.crc".shellCmd,
      s"rm -r -f $localfsStaging/.StackedWaferET$i.csv.crc".shellCmd
    )
    crcClean.ignoreError = true
    crcClean
  }))

}

new WaferETPig()

