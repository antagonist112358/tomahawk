/*
 Copyright 2014 MentalArray, LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/**
 * This class is a wrapper that provides for utilizing Pig to join data sets without having to write an entire
 * script.
 * It utilizes copyMerge functionality, as well as scala-based header and schema cleaning functions to
 * return a directory with part files, as well as a single merged file (.csv as default) on hdfs.
 */
package net.mentalarray.doozie.JobHelpers

import net.mentalarray.doozie.DSL.TextFile
import net.mentalarray.doozie.Utility
import net.mentalarray.doozie.Utility.Path

/**
 *
 * @param job reads in the jobname
 * @param jobConfig reads in the Joiner configuration from the case class.
 *                  This is a template job (slightly different from Workflow Tasks) and as such, cannot receive
 *                  the same sort of function calling and evaluation that a typical workflow script task can take.
 *                  This wrapper enables a WorkflowTemplate instantiation.
 */
class DataSetJoiner(job: String, jobConfig: DataSetJoinerConfiguration) extends WorkflowTemplate(job, jobConfig) with Logging {

  // point to the location on hdfs where the generic table joiner script is located.
  final val _scriptLocation: String = "/apps/scripts/GenericTableJoiner.pig"
  // load some jar files that could be called in on the Pig Script
  final val _jarLibrary: String = """REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/Unstack.jar';
                                      REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/PiggyBank.jar';
                                      REGISTER 'hdfs://yacdevmaster1:8020/apps/libs/datafu-1.2.0.jar';"""

  /**
   * Provide static pointers to the passed in parameters to the class
   */

  val _job = job
  val _firstTableJoinField1 = jobConfig.firstTableJoinField1
  val _firstTableJoinField2 = jobConfig.firstTableJoinField2
  val _secondTableJoinField1 = jobConfig.secondTableJoinField1
  val _secondTableJoinField2 = jobConfig.secondTableJoinField2
  val _firstDataSetDirectory = jobConfig.firstDataSetDirectory
  val _secondDataSetDirectory = jobConfig.secondDataSetDirectory
  val _firstDelimiter = jobConfig.firstDelimiter
  val _secondDelimiter = jobConfig.secondDelimiter
  val _dateFieldFirst = jobConfig.dateFieldFirst
  val _dateFormatting = jobConfig.dateFormatting
  val _daysBackFirst = jobConfig.daysBackFirst
  val _csvStore = jobConfig.csvStoreDirectory
  val _finalDirectory = jobConfig.finalDirectory
  val _joinOption = jobConfig.joinOption
  val _storageDelimiter = jobConfig.storageDelimiter
  /**
   * To effectively join without excessive memory overhead in Pig, it is important to
   * ensure that the field(s) to be joined are not null.
   * Below is case matching for string replacement that will handle joining on either one
   * or two fields.
   */

  val _firstElementNullFilter: String = _firstTableJoinField2 match {
    case "" => "%s is not null".replace("%s", _firstTableJoinField1)
    case _ => "%s is not null".replace("%s", _firstTableJoinField1) +
      " AND %s is not null".replace("%s", _firstTableJoinField2)
  }

  val _secondElementNullFilter: String = _secondTableJoinField2 match {
    case "" => "%s is not null".replace("%s", _secondTableJoinField1)
    case _ => "%s is not null".replace("%s", _secondTableJoinField1) +
      " AND %s is not null".replace("%s", _secondTableJoinField2)
  }

  /**
   * Set the string replacement for the joining string on the first data set.
   */

  // change the replacement test based on number of parameters supplied
  val _firstJoinString = jobConfig.firstTableJoinField2 match {
    case "" => _firstTableJoinField1
    case _ => _firstTableJoinField1 + "," + _firstTableJoinField2
  }
  /** Provide error checking for users potentially providing an invalid declaration
    * (INNER will throw an exception for some reason if it is included.)
    * Allow for all other types to be passed (e.g. LEFT OUTER, RIGHT OUTER, etc.)
    * See Pig documentation for all allowable join types.
    */
  val _joinType = jobConfig.joinType match {
    case "INNER" => ""
    case _ => jobConfig.joinType
  }
  /**
   * Set the string replacement for the join string on the second data set.
   */
  val _secondJoinString = jobConfig.secondTableJoinField2 match {
    case "" => _secondTableJoinField1
    case _ => _secondTableJoinField1 + "," + _secondTableJoinField2
  }

  /**
   * Create a random directory to handle the job store and transfer items.
   */
  val _joinedStorage = Path.combine("/tmp/Pig", java.util.UUID.randomUUID().toString)

  /**
   * Set the final hdfs storage location for the job.
   */
  val _finalStorage = Utility.Path.combine(_finalDirectory, _job)

  /**
   * Create a temp directory on hdfs to handle the regex cleaning of the header and schema files
   */
  val _headerCleanerDir = Utility.Path.combine("/tmp/Pig", java.util.UUID.randomUUID().toString)

  /**
   * Final location of the merged file
   */

  val _csvHDFSStore = Utility.Path.combine(_csvStore, _job)

  /**
   * Run the Pig script with all of the replacement parameters
   */

  appendStep(PigTask { task => task.setScript(TextFile.loadFromHdfs(_scriptLocation))
    task.setScriptReplacements(Replace(
      "jarLibrary" -> _jarLibrary,
      "firstDataSetDirectory" -> _firstDataSetDirectory,
      "firstDelimiter" -> _firstDelimiter,
      "secondDataSetDirectory" -> _secondDataSetDirectory,
      "secondDelimiter" -> _secondDelimiter,
      "firstElementNullFilter" -> _firstElementNullFilter,
      "secondElementNullFilter" -> _secondElementNullFilter,
      "firstJoinString" -> _firstJoinString,
      "joinType" -> _joinType,
      "secondJoinString" -> _secondJoinString,
      "dateFormatting" -> _dateFormatting,
      "joinOption" -> _joinOption,
      "joinedStorage" -> _joinedStorage,
      "storageDelimiter" -> _storageDelimiter,
      "dateFieldFirst" -> _dateFieldFirst,
      "daysBackFirst" -> _daysBackFirst
    ))
    Log.debug(task.script)
  })

  /**
   * copy, move header/schema, cleanup the schema, then remove the temp directories.
   */

  appendStep(HdfsTask { task => task.setCommand("rm -r -f " + _finalStorage)})
  appendStep(HdfsTask { task => task.setCommand("rm -r -f " + _csvHDFSStore + ".csv")})
  appendStep(HdfsTask { task => task.setCommand("cp " + _joinedStorage + " " + _finalStorage)})
  appendStep(HdfsTask { task => task.setCommand("rm -f " + _finalStorage + "/.pig_header")})
  appendStep(HdfsTask { task => task.setCommand("rm -f " + _finalStorage + "/.pig_schema")})
  appendStep(HdfsTask { task => task.setCommand("mkdir " + _headerCleanerDir)})
  appendStep(HdfsTask { task => task.setCommand("cp " + _joinedStorage + "/.pig_header" + " " + _headerCleanerDir + "/.pig_header")})
  appendStep(HdfsTask { task => task.setCommand("cp " + _joinedStorage + "/.pig_schema" + " " + _headerCleanerDir + "/.pig_schema")})
  appendStep(ScalaTask {
    val inFile = _headerCleanerDir + "/.pig_header"
    val outFile = _finalStorage + "/.pig_header"
    val headerClean = ".*::".r
    val overWriteExisting = false
    SchemaCleaner.parsePigHeader(inFile, outFile, headerClean, overWriteExisting)
    true
  })
  appendStep(FileBuilderTask("Joiner") { task =>
    task.inPath(_finalStorage)
      .outPath(_csvHDFSStore + ".csv")
      .srcCheckDel(false)
      .srcSys("hdfs")
      .destSys("hdfs")
  })
  appendStep(ScalaTask {
    val inFile = _headerCleanerDir + "/.pig_schema"
    val outFile = _finalStorage + "/.pig_schema"
    val schemaClean = ":\".*::".r
    val replacement = ":\""
    val overWriteExisting = false
    SchemaCleaner.parsePigSchema(inFile, outFile, schemaClean, replacement, overWriteExisting)
    true
  })
  appendStep(HdfsTask { task => task.setCommand("rm -r -f " + _headerCleanerDir)})
  appendStep(HdfsTask { task => task.setCommand("rm -r -f " + _joinedStorage)})
}

/**
 * Script supplied elements for using this class:
 * @param firstTableJoinField1 Left data set Join Field #1 (required)
 * @param firstTableJoinField2  Left data set Join Field #2 (optional, required if Right supplied)
 * @param secondTableJoinField1 Right data set Join Field #1 (required)
 * @param secondTableJoinField2 Right data set Join Field #2 (optional, required if Left supplied)
 * @param firstDataSetDirectory Location of left data set on hdfs
 * @param secondDataSetDirectory Location of right data set on hdfs
 * @param firstDelimiter Delimiter type of left data set
 * @param secondDelimiter Delimiter type of right data set
 * @param dateFieldFirst Field name of date filtering on left data set
 * @param dateFormatting Standard formatting string of the dateFieldFirst field
 * @param daysBackFirst Integer - number of days back from today for filtering the first data set.
 * @param finalDirectory Hdfs directory location of where to store the file.
 * @param csvStoreDirectory Hdfs directory location of where to store a merged file for hosting.
 * @param joinType Pig option - type of join (e.g. FULL OUTER, LEFT OUTER, etc.)
 * @param joinOption - Pig option - optional join types (e.g. replicated)
 * @param storageDelimiter Delimiter type for the final storage table.
 */
case class DataSetJoinerConfiguration(
                                       firstTableJoinField1: String,
                                       firstTableJoinField2: String,
                                       secondTableJoinField1: String,
                                       secondTableJoinField2: String,
                                       firstDataSetDirectory: String,
                                       secondDataSetDirectory: String,
                                       firstDelimiter: String,
                                       secondDelimiter: String,
                                       dateFieldFirst: String,
                                       dateFormatting: String,
                                       daysBackFirst: Int,
                                       finalDirectory: String,
                                       csvStoreDirectory: String,
                                       joinType: String,
                                       joinOption: String,
                                       storageDelimiter: String
                                       ) extends TemplateConfiguration

