package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/7/14.
 */
class VariablesMapSpec extends Specification {

  val test = new VariablesMap

  "WorkflowArgs should be able to" should {

    // Test retrieval
    "Return key/value pairs for String" in {
      val testVal = "Value"
      test.store("String", testVal)

      val value: String = test.retrieve("String")

      value must be equalTo(testVal)
    }

    "Return key/value pairs for AnyVal" in {
      val testVal: Int = 10

      test.store("AnyVal", testVal)

      val value: Int = test.retrieve("AnyVal")

      value must be equalTo(testVal)
    }

  }


}
