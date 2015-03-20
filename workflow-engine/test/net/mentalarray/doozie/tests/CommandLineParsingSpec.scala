package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 10/16/14.
 */
class CommandLineParsingSpec extends Specification {

  // Run tests sequentially
  sequential


  "ApplicationOptions.parseArgs" should {

    "fail with no arguments" in {

      val resultantMap = ApplicationOptions.parseArgs(Array.empty[String])

      resultantMap.size must beEqualTo(0)
    }

    "fail when improper number of arguments are specified" in {
      val args = Array[String]("--Path", "~/FabTrackingScalding.jar", "--instancename")

      ApplicationOptions.parseArgs(args) must throwA[Exception]
    }

    "pass when proper args are provided" in {
      val scalaArgs = Array[String]("--path", "~/FabTracking.scala")
      val jarArgs = Array[String]("--path", "sample-workflows/target/scala-2.10/sample-workflows.jar", "--instancename", "net.mentalarray.doozie.examples.FabTrackingWorkflow")

      val optsScala = ApplicationOptions.parseArgs(scalaArgs)
      val optsJar = ApplicationOptions.parseArgs(jarArgs)

      optsScala.size must beEqualTo(1)
      optsScala("scriptPath") must beEqualTo("~/DataTracking.scala")

      optsJar.size must beEqualTo(2)
      optsJar("instanceName") must beEqualTo("net.mentalarray.doozie.examples.DataFlow")
    }

    "fail when correct number but improperly formatted args are provided" in {
      val wrongJar = Array[String]("--path", "~/FabTrackingScalding.cpp", "--instancename", "net.mentalarray.doozie.fabtracking")
      val wrongCmdline = Array[String]("--path", "~/FabTrackingScalding.jar", "--instancename", "--console", "net.mentalarray.doozie.fabtracking")

      ApplicationOptions.parseArgs(wrongJar) must throwA[Exception]

      ApplicationOptions.parseArgs(wrongCmdline) must throwA[Exception]
    }

    "provide the path to the scala script for scala mode" in {
      val scalaArgs = Array[String]("--path", "~/FabTracking.scala")
      val expected = "~/FabTracking.scala"

      true must beTrue
    }

    "provide the jarPath and instancename for jar mode" in {
      val jarArgs = Array[String]("--path", "~/DataTracking.jar", "--instancename", "net.mentalarray.doozie.fabtracking")
      val expectedPath = "~/DataTracking.jar"
      val expectedInstance = "net.mentalarray.doozie.datatracking"

      true must beTrue
    }

    "handles two switches with no arguments in one line in any order" in {
      val firstOrder = Array[String]("--test", "--console")
      val secondOrder = Array[String]("--conSole", "--TEST")

      val firstResult = ApplicationOptions.parseArgs(firstOrder)
      val secondResult = ApplicationOptions.parseArgs(secondOrder)

      firstResult("testingOnly") must beEqualTo(true)
      firstResult("logToConsole") must beEqualTo(true)

      secondResult("testingOnly") must beEqualTo(true)
      secondResult("logToConsole") must beEqualTo(true)
    }

    "fails with unknown argument" in {
      val test1 = Array[String]("--bong")
      val test2 = Array[String]("--testing")

      ApplicationOptions.parseArgs(test1) must throwA[Exception]
      ApplicationOptions.parseArgs(test2) must throwA[Exception]
    }

  }

}
