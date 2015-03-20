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

import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    "ScalaTools snapshots at Sonatype" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "Concurrent Maven Repo" at "http://conjars.org/repo", // For Scalding, Cascading etc
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Pivotal HD / SpringSource" at "http://repo.springsource.org/plugins-release/", // For Hadoop, Pig, Sqoop, etc.
    "JBoss" at "http://repository.jboss.org/nexus/content/groups/public/",
    "Nexus" at "http://105.193.30.75:8081/"
  )

  object V {
    val scalding  = "0.11.2"
    val scalding_jdbc  = "0.9.0"
    val spark = "1.1.0"
    val specs2    = "2.3.13" // -> "1.13" when we bump to Scala 2.10.0
    val cascading = "2.5.5"
    val nscala_time = "1.2.0"
    val scopt = "3.2.0"
    val twitterUtil = "6.12.1"
    val slf4j = "1.6.1"
    val log4j = "1.2.16"
    val jodaTime = "2.5"

    // Pivotal crap
    val hadoop = "2.2.0-gphd-3.0.1.0"
    val hive = "0.12.0-gphd-3.0.1.0"
    val sqoop = "1.4.2-gphd-3.0.1.0"
    val pig = "0.12.0-gphd-3.0.1.0"
    val scala = "2.10.4"
    // Akka
    val akka = "2.2.4"
  }

  object Libraries {
    // Hadoop components
    val hadoopCore        = "org.apache.hadoop"   %   "hadoop-common"                 % V.hadoop       % "provided" intransitive()
    val hadoopClientCore  = "org.apache.hadoop"   %   "hadoop-mapreduce-client-core"  % V.hadoop       % "provided" intransitive()
    val hadoopYarnClient  = "org.apache.hadoop"   %   "hadoop-yarn-client"            % V.hadoop       % "provided"
    val hadoopYarnApi     = "org.apache.hadoop"   %   "hadoop-yarn-api"               % V.hadoop       % "provided"
    val hadoopYarnCommon  = "org.apache.hadoop"   %   "hadoop-yarn-common"            % V.hadoop       % "provided"
    val sqoopLib          = "org.apache.sqoop"    %   "sqoop"                         % V.sqoop        % "provided"
    val pigLib            = "org.apache.pig"      %   "pig"                           % V.pig          % "provided"
    val sparkCore         = "org.apache.spark"    %   "spark-core_2.10"               % V.spark        % "provided" intransitive()
    val sparkYarn         = "org.apache.spark"    %   "spark-yarn_2.10"               % V.spark        % "provided" //intransitive()

    // Cascading / Scalding libraries
    val cascadingCore     = "cascading"           %   "cascading-core"          % V.cascading
    val cascadingLocal    = "cascading"           %   "cascading-local"         % V.cascading
    val cascadingHadoop   = "cascading"           %   "cascading-hadoop2-mr1"   % V.cascading
    val scaldingCore      = "com.twitter"         %   "scalding-core_2.10"      % V.scalding        exclude( "cascading", "cascading-local" ) exclude( "cascading", "cascading-hadoop" )
    val scaldingJDBC      = "com.twitter"         %   "scalding-jdbc_2.10"      % V.scalding_jdbc
    val hiveJDBC          = "org.apache.hive"     %   "hive-jdbc"               % V.hive            % "provided" intransitive() //exclude( "commons-lang", "commons-lang" ) exclude("jackson-core","jackson-core") exclude("jackson-mapper","jackson-mapper")

    // Add additional libraries go here
    val log4j             = "log4j"                   % "log4j"                 % V.log4j
    val scopt             = "com.github.scopt"        %%  "scopt"               % V.scopt
    val twitterEval       = "com.twitter"             %%  "util-eval"           % V.twitterUtil
    val specs2            = "org.specs2"              %%  "specs2"              % V.specs2      % "test"
    val nscala_time       = "com.github.nscala-time"  %%  "nscala-time"         % V.nscala_time
    val joda_time         = "joda-time"               %  "joda-time"            % V.jodaTime
    val akka_actors       = "com.typesafe.akka"       %  "akka-actor"           % V.akka 
    val scalaReflection   = "org.scala-lang"          %  "scala-reflect"        % V.scala

  }
}
