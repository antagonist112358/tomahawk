package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/8/14.
 */
class SqoopTaskSpec extends Specification {

  private def getStateInstance : SqoopTask = {
    SqoopTask(
      _.connect("java:connection:string")
      .username("user")
      .password("pass")
      .targetDir("/user/temp/output")
    )
  }

  "SqoopWorkflowState" should {

    "Be configurable through an anonymous companion object" in {
      val state = SqoopTask(
        _.connect("java:connection:string")
        .query("select * from table")
      )

      state must not beNull
    }

    "throw an exception when connection string is missing" in {
      val state = SqoopTask(
        _.query("select * from table")
        .username("user")
        .password("password")
      )

      state.validate must throwA[WorkflowStateException]
    }

    "throw an exception when target-directory is missing" in {
      val state = SqoopTask(
        _.query("select * from table")
        .connect("java:connection:string")
        .username("user")
        .password("password")
      )

      state.validate must throwA[WorkflowStateException]
    }

    "throw an exception when both query and table are not specified" in {
      val state = SqoopTask(
        _.connect("java:connection:string")
        .username("user")
        .password("password")
        .targetDir { "/user/temp/output" }
      )

      state.validate must throwA[WorkflowStateException]
    }

    "allow either the query or the table to be specified" in {
      val state = getStateInstance

      // Check query
      state.query("select * from table")
      state.validate must not(throwA[Exception])

      // Set table and clear query
      state.query(null)
      .table("someTable")

      state.validate must not(throwA[Exception])
    }

    "not allow both the query and the table to be set" in {
      val state = getStateInstance

      // Set both
      state.query("select * from table")
      .table("table")

      state.validate must throwA[WorkflowStateException]
    }
  }

}
