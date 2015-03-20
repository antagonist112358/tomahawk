/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mentalarray.doozie.DSL

/**
 * TextFile is a utility / helper object for the Dataflow DSL. It is used to load text files from either HDFS or the
 * local filesystem.
 */
object TextFile {

  /**
   * Reads a text file from the local filesystem.
   * @param path The full path to the text file to read.
   * @return The contents of the target text file, as a string.
   */
  def loadFromLocal(path: String) : String = {
    Path.readFile(path)
  }

  /**
   * Reads a text file from HDFS.
   * @param path The full path to the text file to read. Does not require the 'hdfs://' prefix on the path.
   * @return The contents of the target text file, as a string.
   */
  def loadFromHdfs(path: String) : String = {
    Hadoop.readFile(path)
  }

  /**
   * Writes the contents of a string to a file in HDFS.
   * @param path The path to the output / target file. Does not require the 'hdfs://' prefix on the path.
   * @param data The string to write out to the target file.
   * @param overWrite Indicates whether the target file should be overwritten. This flag is
   *                  is ignored if the 'appendIfExists' flag is set to true. This method will throw an exception
   *                  if the overwrite flag is false and the target file already exists. Default value is false.
   * @param appendIfExists Indicates whether the target file should be appended to if it already exists. The default
   *                       value is false.
   */
  def writeToHdfs(path: String, data: String, overWrite: Boolean = false,
                  appendIfExists: Boolean = false) : Unit = {
    Hadoop.writeFile(path, data, overWrite)
  }
}
