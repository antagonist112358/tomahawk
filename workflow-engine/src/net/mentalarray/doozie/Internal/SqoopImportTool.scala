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
package net.mentalarray.doozie.Internal

import com.cloudera.sqoop.SqoopOptions

/**
 * The SqoopImportTool extends the ImportTool class, and allows for the protected method:
 * 'validateImportOptions' to be invoked via the public 'validate' method.
 * @note This class only exists to allow for Sqoop tasks to validate their configuration properly as a separate action,
 *       without requiring the class be invoked or executed.
 */
class SqoopImportTool extends com.cloudera.sqoop.tool.ImportTool {

  /**
   * Validates a 'SqoopOptions' configuration instance against Sqoop.
   * @param in The SqoopOptions class to validate.
   */
  def validate(in: SqoopOptions) = {
    super.validateImportOptions(in)
  }
}
