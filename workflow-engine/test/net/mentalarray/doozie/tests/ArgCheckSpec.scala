package net.mentalarray.doozie.tests

import net.mentalarray.doozie.ArgCheck
import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/27/14.
 */
class ArgCheckSpec extends Specification {

  // Run tests sequentially
  sequential

  "ArgCheck" should {

    "fail with no arguments" in {
      ArgCheck.suppressUsage

      ArgCheck.setArgs(Array[String]("")) must beFalse
    }

    "fail when improper number of arguments are specified" in {
      val args = Array[String]("--path", "~/TestingScalding.jar", "--instancename", "net.mentalarray.doozie.Fallthrough")

      ArgCheck.suppressUsage

      ArgCheck.setArgs(args.take(1)) must beFalse
      ArgCheck.setArgs(args.take(3)) must beFalse
    }

    "pass when proper args are provided" in {
      val scalaArgs = Array[String]("--path", "~/DataTracking.scala")
      // --path sample-workflows/target/scala-2.10/sample-workflows.jar --instancename net.mentalarray.doozie.examples.FabTrackingWorkflow
      val jarArgs = Array[String]("--path", "sample-workflows/target/scala-2.10/sample-workflows.jar", "--instancename", "net.mentalarray.doozie.examples.Haduken")

      ArgCheck.suppressUsage

      // Check scala args
      ArgCheck.setArgs(scalaArgs) must beTrue
      // Check jar args
      ArgCheck.setArgs(jarArgs) must beTrue
    }

    "fail when correct number but improperly formatted args are provided" in {
      val wrongScala = Array[String]("--path", "~/HelloWorld.jar")
      val wrongJar = Array[String]("--path", "~/HelloScala.scala", "--instancename", "net.mentalarray.doozie.fabtracking")

      ArgCheck.suppressUsage

      // Check incorrect scala args
      ArgCheck.setArgs(wrongScala) must beFalse
      // Check incorrect jar args
      ArgCheck.setArgs(wrongJar) must beFalse

    }

    "provide the path to the scala script for scala mode" in {
      val scalaArgs = Array[String]("--path", "~/FabTracking.scala")
      val expected = "~/FabTracking.scala"

      ArgCheck.suppressUsage
      ArgCheck.setArgs(scalaArgs)

      ArgCheck.fileName must beEqualTo(expected)
    }

    "provide the jarPath and instancename for jar mode" in {
      val jarArgs = Array[String]("--path", "~/DataTracking.jar", "--instancename", "net.mentalarray.doozie.whitewall")
      val expectedPath = "~/DataTracking.jar"
      val expectedInstance = "net.mentalarray.doozie.fabtracking"

      ArgCheck.suppressUsage
      ArgCheck.setArgs(jarArgs)

      // Check filename
      ArgCheck.fileName must beEqualTo(expectedPath)
      // Check instanceName
      ArgCheck.instanceName must beEqualTo(Some(expectedInstance))
    }
  }

}
