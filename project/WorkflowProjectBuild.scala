/*
 * Copyright (c) 2012 SnowPlow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import sbt.Keys._
import sbt._

object WorkflowProjectBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import org.sbtidea.SbtIdeaPlugin.ideaExcludeFolders

  // Configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > "}
  }

  lazy val root = Project("dataflow", file("."))
    .settings(ideaExcludeFolders := ".idea" :: ".idea_modules" :: Nil)
    .aggregate(dataflowServer, workflowEngine)

  lazy val dataflowServer = Project("workflow-server", file("workflow-server"))
    .settings(dataflowServerSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        Libraries.specs2,
        Libraries.log4j
      )
    )
    .dependsOn(workflowEngine)

  lazy val macroProject = Project("macros", file("macros"))
    .settings(macroSettings: _*)
    .settings(libraryDependencies ++= Seq(
      Libraries.scalaReflection 
    ))

  // Define our project, with basic project information and library dependencies
  lazy val workflowEngine = Project("dataflow-engine", file("workflow-engine"))
    .settings(buildSettings: _*)
    .settings(
      sources in (Compile,doc) := Seq.empty,
      publishArtifact in packageDoc := false,
      publishArtifact in (Compile,packageDoc) := false )
    .settings(
      libraryDependencies ++= Seq(
        Libraries.cascadingCore,
        Libraries.cascadingLocal,
        Libraries.cascadingHadoop,
        Libraries.scaldingCore,
        Libraries.scaldingJDBC,
        Libraries.hadoopCore,
        Libraries.hadoopClientCore,
        Libraries.hadoopYarnClient,
        Libraries.hadoopYarnApi,
        Libraries.hadoopYarnCommon,
        Libraries.specs2,
        Libraries.hiveJDBC,
        Libraries.twitterEval,
        Libraries.nscala_time,
        Libraries.joda_time,
        Libraries.sqoopLib,
        Libraries.pigLib,
        Libraries.sparkCore,
        Libraries.sparkYarn
      )
    )
    .dependsOn(macroProject)


}
