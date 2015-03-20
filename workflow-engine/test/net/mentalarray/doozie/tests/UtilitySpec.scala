package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/11/14.
 */
class UtilitySpec extends Specification {

  "Activator" should {
    "allow creation of types by class specification" in {
      val created = Activator[String].createInstance(classOf[String])
      created.getClass.getName must be equalTo "java.lang.String"
    }
  }

  "Hadoop.WriteFile" should {
    "write a file to hdfs" in {
      val data = "Test1, Test2, Test3"
      val filePath = "/tmp/" + java.util.UUID.randomUUID().toString

      Utility.Hadoop.writeFile(filePath, data) must not(throwA [Exception])

    }
    "throw an exception when trying to overwrite an existing file" in {
      val data = "Test1, Test2, Test3"
      val filePath = "/tmp/" + java.util.UUID.randomUUID().toString

      Utility.Hadoop.writeFile(filePath, data) must not(throwA [Exception])
      Utility.Hadoop.writeFile(filePath, data) must throwA [Exception]
    }
    "match input from Hadoop.ReadFile" in {
      val data = "Test1, Test2, Test3"
      val filePath = "/tmp/" + java.util.UUID.randomUUID().toString

      Utility.Hadoop.writeFile(filePath, data) must not(throwA [Exception])
      val returnData = Utility.Hadoop.readFile(filePath)
      returnData must be equalTo data
    }
  }
}
