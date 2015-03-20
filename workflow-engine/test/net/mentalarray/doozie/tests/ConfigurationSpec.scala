package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by bgilcrease on 9/8/14.
 */
class ConfigurationSpec extends Specification {


  Config.setDBConfiguration("ERD 20nm")
  Config.clearTaskParameters("SqoopImport")

  "Configuration " should {
    // Test setting and retrieval
    "Set and retrieve configuration of a string" in {
      val name =  "My name is"
      Configuration("SqoopImport","name") = name
      val result: String = Config[String]("SqoopImport","name")
      result must be equalTo(name)
    }
    "Set and retrieve configuration of an Int" in {
      val count = 50
      Configuration("SqoopImport","count") = count
      val result: Int = Config[Int]("SqoopImport","count")
      result must be equalTo(count)
    }

  }


  "DBStatus" should {
    "Retrieve job status from yacetl db" in {
      val param: String = DBStatus.getStatus("ERD 20nm", "SqoopImport")
      param must not be equalTo(null)
    }
    "Create a new job/task if entry doesn't exist" in {
      val job: String = "TestJob"
      val task: String = "TestTask"
      val status: String = "Fail"
      DBStatus.setStatus(job,task,status)
      val result = DBStatus.getStatus(job, task)
      result must be equalTo(status)
    }
    //    "Start a task" in {
    //      val dbTest  = new DBStatus()
    //      dbTest.startTask("ERD 20nm", "SqoopImport")
    //      val status : String = dbTest.getStatus("ERD 20nm", "SqoopImport")
    //      status must be equalTo("Running")
    //    }
    "End a task with Success" in {
      DBStatus.endTask("ERD 20nm", "SqoopImport", pass=true)
      val status : String = DBStatus.getStatus("ERD 20nm", "SqoopImport")
      status must be equalTo("Pass")
    }
    //    "End a task with Failure" in {
    //      val dbTest  = new DBStatus()
    //      dbTest.endTask("ERD 20nm", "SqoopImport", pass=false)
    //      val status : String = dbTest.getStatus("ERD 20nm", "SqoopImport")
    //      status must be equalTo("Fail")
    //    }
  }

  val dbTest  = new DBConfiguration("ERD 20nm", JDBCConnection().HiveMetastore)

  "DBConfiguration " should {
    "Set and retrieve paramaters from yacetl db" in {
      val time = "i'll do it later"
      dbTest.store("SqoopImport", "time", time)
      val param: String = dbTest.retrieve("SqoopImport", "time")
      param must be equalTo(time)
    }
    "Throw exception if parameter does not exist" in {
      dbTest.retrieve("SqoopImport", "DNE") must throwA[Exception]
    }
//    "Throw exception if job does not exist" in {
//      dbTest.getParam( "SqoopImport", "end") must throwA[Exception]
//    }
    "Throw exception if task does not exist" in {
      dbTest.retrieve("DNE", "end") must throwA[Exception]
    }
  }

}
