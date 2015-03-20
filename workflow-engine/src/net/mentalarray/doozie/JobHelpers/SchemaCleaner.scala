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

import scala.util.matching.Regex

/**
 * The object is used to implement methods for modifying the generated .pig_header
 * and .pig_schema files to remove origination reference of field names. The methods
 * of parsePigHeader and parsePigSchema act upon the private method parser.  Since
 * header files are simpler by nature, the option is left to utilize a general
 * 'replace with null' function, whereas the Schema file needs to have a more complex
 * matching and replacing function applied to it.
 * The final action after parsing will write the modified file back to hdfs in the
 * supplied location.
 */

object SchemaCleaner {

  /**
   *
   * @param inputFile non-URI String pointing to file on hdfs "/tmp/data/user/.pig_header"
   * @param outputPath non-URI String pointing to file on hdfs "/tmp/data_final/.pig_header"
   * @param regexString job-specific provided regex that will perform the matching. ".*\\w{2}$".r
   * @param replacementString job-specific provided String that will replace the matched elements
   *                          from the regex. "test"
   * @param overWrite Boolean command for overwriting a file that already exists in the location
   *                  provided with the parameter outputPath
   * @param delim String delimiter of the source and destination file.
   */

  // Define Generic private method parser.

  private def parser(inputFile: String, outputPath: String, regexString: Regex,
                     replacementString: String, overWrite: Boolean = false, delim: String = ","): Unit = {

    /** Set a value that assembles a fully qualified reference to a file on hdfs.
      * TextFile.loadFromHdfs method allows for passing in a string and retrieving
      * the file specified.
      */

    val schemaLoad = TextFile.loadFromHdfs(inputFile)

    val regex = regexString

    /**
     * Split the input file by the provided delimiter, then map each of the split strings to perform
     * a regex replaceAllIn function.  Transform the array[String] back to a delimited String, then
     * write the file to HDFS using a Streaming Function within TextFile.writeToHdfs.
     */

    val cleaner = schemaLoad.split(delim)
      .map(s => regex.replaceAllIn(s, replacementString))
      .mkString(delim)

    TextFile.writeToHdfs(outputPath, cleaner, overWrite)

  }

  /**
   * public method: parsePigHeader for providing a default null replacement for the matched regex.
   */

  def parsePigHeader(inputFile: String, outputPath: String, regexString: Regex,
                     overWrite: Boolean = false): Unit = {

    parser(inputFile, outputPath, regexString, "", overWrite)

  }

  /**
   * public method: parsePigSchema for allowing for a defined replacement string for the matched regex.
   */
  def parsePigSchema(inputFile: String, outputPath: String, regexString: Regex,
                     replacementString: String, overWrite: Boolean = false): Unit = {

    parser(inputFile, outputPath, regexString, replacementString, overWrite)

  }
}