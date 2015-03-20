package net.mentalarray.doozie.JobHelpers

/**
 * Created by Ben Marrs on 3/2/15.
 * Used for taking the output from a Pig Job and creating either a "cleaned schema" hosted directory, or
 * a cleaned directory and a csv file representing the aggregation of the data within the directory.
 */
object StaticDataBuilder {


  private def FileCleaner(inputDirectory: String, errorIgnore: Boolean = false, recurseChk: String = "-r", forceChk: String = "-f") = {
    val directoryCleaner = HdfsTask(_.setCommand(s"rm $recurseChk $forceChk $inputDirectory"))

    directoryCleaner.ignoreError = errorIgnore
    directoryCleaner
  }

  private def TempShuffle(tmpDir: String, finalDir: String) = {
    val shuffle = Tasks(
      s"cp $tmpDir $finalDir".hdfsCmd,
      s"rm -f $finalDir/.pig_header".hdfsCmd,
      s"rm -f $finalDir/.pig_schema".hdfsCmd
    )
    shuffle
  }

  private def HeaderTransfer(tmpDir: String, finalDir: String) = {
    val headerCleanRegex = ".*::".r
    ScalaTask {
    SchemaCleaner.parsePigHeader(s"$tmpDir/.pig_header", s"$finalDir/.pig_header", headerCleanRegex)
      true
    }
  }

  private def SchemaTransfer(tmpDir: String, finalDir: String) = {
    val schemaCleanRegex = ":\".*::".r
    val schemaReplace = ":\""
    ScalaTask {
      SchemaCleaner.parsePigSchema(s"$tmpDir/.pig_schema", s"$finalDir/.pig_schema", schemaCleanRegex, schemaReplace)
      true
    }
  }

  private def CSVBuilder(jobName: String, finalDir: String, csvPath: String) = {
    FileBuilderTask(jobName) { _.inPath(finalDir).outPath(csvPath)}
  }


  def FileGenerator (tempDirectory: String, finalDirectory: String, fileName: String, csvLocationFull: String ) = {
    Tasks(
    FileCleaner(inputDirectory = finalDirectory, errorIgnore = true),
    FileCleaner(inputDirectory = csvLocationFull),
    TempShuffle(tempDirectory, finalDirectory),
    HeaderTransfer(tempDirectory, finalDirectory),
    CSVBuilder(fileName, finalDirectory, csvLocationFull),
    SchemaTransfer(tempDirectory, finalDirectory),
    FileCleaner(tempDirectory)
    )
  }

  def DirectoryOnlyGenerator (tempDirectory: String, finalDirectory: String) = {
    Tasks(
    FileCleaner(inputDirectory = finalDirectory, errorIgnore = true),
    TempShuffle(tempDirectory, finalDirectory),
    HeaderTransfer(tempDirectory, finalDirectory),
    SchemaTransfer(tempDirectory, finalDirectory),
    FileCleaner(tempDirectory)
    )
  }

}

