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

package net.mentalarray.doozie.JobHelpers

/**
 * The purpose of this object is to permit a method for re-basing Hive imports that are importing data
 * on a fixed schedule.  Scheduled jobs that import to a directory will create copies of part files
 * which could potentially increase the reserved disk space.  This method allows for a concatenation
 * of all of the part files within a directory in accordance with the specified storage partition schema.
 */

object HiveJobHelper {

// Set the default parameters for how the table creation will be handled. (utilizing Snappy Compression here)


  val tableCreateparam : String =
    "SET hive.exec.max.dynamic.partitions.pernode=10000;" +
    "SET mapred.max.split.size=256000000;" +
    "SET hive.exec.compress.output=true;" +
    "SET mapred.output.compression.type=BLOCK;" +
    "SET mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;" +
    "SET hive.exec.dynamic.partition.mode=nonstrict;" +
    "SET hive.exec.dynamic.partition=true;" +
    "SET hive.enforce.bucketing = true;"

  /**
    * @param hiveSchema supply the source hive database
    * @param tempTbl supply a name for a temporary table to be created to handle the internal restructuring
    * @param sourceTbl supply the source table name to be restructured.
    * @param partitionfield1 supply the first partition field
    * @param partitionfield2 supply the second partition field
    * @param monthString supply the date-specified partition string
    * @return builds the hive sequence list to insert into a hiveTask
   *
   *         First step is to define the database, then create a temp table that replicates the sourceTable
   *         with the provided partition field in a where clause.
   *         The source table is then truncated on the same partition that has been copied over to the temp table.
   *         With settings applied from tableCreateparam, the data is then copied from the temp table back to the
   *         source table with partition scheme of the original source table.
   *         Lastly, the temp table is dropped from the hive Metastore.
    */

  def hiveRebaseFormat ( hiveSchema: String, tempTbl: String, sourceTbl: String,
                         partitionfield1: String, partitionfield2: String, monthString: String ) : List[String] = {
    (
      ("use " + hiveSchema + ";" +
      "CREATE TABLE " + tempTbl + " AS SELECT * FROM " + sourceTbl + " WHERE " + partitionfield1 + "='%s';" +
      "TRUNCATE TABLE " + sourceTbl + " PARTITION ( " + partitionfield1 + " = '%s');" +
      tableCreateparam +
      "INSERT INTO TABLE " + sourceTbl + " PARTITION ( " + partitionfield2 + " , " + partitionfield1 + " ) SELECT * FROM " + tempTbl + " ;" +
      "DROP TABLE " + tempTbl) format (monthString, monthString)
      ).split(";").toList
  }

}
