import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._

object BuildSettings {

  // Settings for Hadoop
  lazy val hadoopSettings = Defaults.defaultSettings ++ Seq[Setting[_]](
    fullClasspath in Test ++= System.getProperty("java.class.path").split(java.io.File.pathSeparator).map { path =>
      Attributed.blank(file(path))
    }.toList
  )

  // Basic settings for our app
  lazy val basicSettings = hadoopSettings ++ Seq[Setting[_]](
    organization  := "MentalArray, LLC",
    version       := "0.3.0",
    description   := "DataFlow - ETL Workflow Engine",
    scalaVersion  := "2.10.4",
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    resolvers     ++= Dependencies.resolutionRepos,
    publishTo <<= version { v: String =>
      val nexus = "http://105.193.30.75:8081/nexus/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "content/repositories/releases")
    },
    credentials += Credentials("Sonatype Nexus Repository Manager", "105.193.30.75", "admin", "P@ssw0rd"),
    // Spec2
    scalacOptions in Test ++= Seq("-Yrangepos"),
    scalaSource in Test := baseDirectory.value / "test",
    scalaSource in Compile := baseDirectory.value / "src",
    resourceDirectory in Compile := baseDirectory.value / "resource",
    mainClass in (Compile, run) := Some("net.mentalarray.doozie.EntryPoint"),
    unmanagedBase := baseDirectory.value / "../lib"
  )


  //publish assembly
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.copy(`classifier` = Some("assembly"))
  }
  
  addArtifact(artifact in (Compile, assembly), assembly)

  // sbt-assembly settings for building a fat jar
  lazy val sbtAssemblySettings = assemblySettings ++ Seq(

    // Slightly cleaner jar name
    jarName in assembly := { name.value + "-" + version.value + ".jar" } ,
    test in assembly := {},
    mainClass in assembly := Some("net.mentalarray.doozie.EntryPoint"),

    // Change the target output directory and the classes output
    target in assembly := baseDirectory.value / "../artifacts",

    // Handle mis-matched versions
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
      {
        case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
        case "application.conf" => MergeStrategy.concat
        case "unwanted.txt"     => MergeStrategy.discard
        case x => old(x)
      }
    },
    // Drop these jars
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set(
        "jsp-api-2.1-6.1.14.jar",
        "jsp-2.1-6.1.14.jar",
        "jasper-compiler-5.5.12.jar",
        "minlog-1.2.jar", // Otherwise causes conflicts with Kyro (which bundles it)
        "janino-2.5.16.jar", // Janino includes a broken signature, and is not needed anyway
        "commons-beanutils-core-1.8.0.jar", // Clash with each other and with commons-collections
        "commons-beanutils-1.7.0.jar",      // "
        "hadoop-core-1.1.2.jar",
        "hadoop-tools-1.1.2.jar", // "
        "guava-15.0.jar",
        "commons-logging-1.1.1.jar",
        "jackson-core-asl-1.7.3.jar",
        "jackson-mapper-asl-1.7.3.jar",
        "objenesis-1.2.jar",
        "mockito-all-1.8.2.jar",
        "datanucleus-api-jdo-3.2.1.jar",
        "datanucleus-rdbms-3.2.1.jar",
        "netty-3.2.2.Final.jar",
        "core-3.1.1.jar", // Conflict with datanucleus
        "avro-1.7.4.jar"  // Conflict with Hive 0.12.0
      )

      cp filter { jar => excludes(jar.data.getName) /*&& excludeDocsAndSources(jar.data.getName)*/ }

    })

  // Don't include tests in final jar
  publishArtifact in GlobalScope in Test := false

  lazy val buildSettings = basicSettings ++ sbtAssemblySettings

  // Settings for the pig-server project
  lazy val dataflowServerSettings = Defaults.defaultSettings ++ Seq[Setting[_]] (
    organization  := "MentalArray, LLC",
    version       := "0.1.0",
    description   := "DataFlow - ETL Workflow Engine: PigServer",
    scalaVersion  := "2.10.4",
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    scalaSource in Compile := baseDirectory.value / "src",
    jarName in assembly := { name.value + "-" + version.value + ".jar" },
    artifactName := {  (sv, module, artifact) => artifact.name + "." + artifact.extension },
    artifactPath in (Compile, packageBin) ~= { defaultPath => 
	    file("./artifacts") / defaultPath.getName
    }
  )

  // Seperate project for macro builds
  lazy val macroSettings = Defaults.defaultSettings ++ Seq[Setting[_]] (
    organization  := "MentalArray, LLC",
    version       := "0.1.0",
    description   := "DataFlow - ETL Workflow Engine: Macro Build",
    scalaVersion  := "2.10.4",
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    scalaSource in Compile := baseDirectory.value / "src",
    jarName in assembly := { name.value + "-" + version.value + ".jar" },
    artifactName := {  (sv, module, artifact) => artifact.name + "." + artifact.extension },
    artifactPath in (Compile, packageBin) ~= { defaultPath => 
	    file("./artifacts") / defaultPath.getName
    }
  )
}
