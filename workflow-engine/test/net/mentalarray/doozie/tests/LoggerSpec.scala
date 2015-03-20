package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/12/14.
 */
class LoggerSpec extends Specification {

  class DummyClass {

  }

  "Logger trait when applied to a class" should {
    val loggableClass = new DummyClass with Logging

    "allow for debug messaging" in {
      loggableClass.Log.debug("Test")
      true must be equalTo true
    }

    "allow for info messaging" in {
      loggableClass.Log.info("Test")
      true must be equalTo true
    }

    "allow for warn messaging" in {
      loggableClass.Log.warn("Test")
      true must be equalTo true
    }

    "allow for error messaging" in {
      loggableClass.Log.error("Test")
      true must be equalTo true
    }

    "allow for fatal messaging" in {
      loggableClass.Log.fatal("Test")
      true must be equalTo true
    }


  }

}
