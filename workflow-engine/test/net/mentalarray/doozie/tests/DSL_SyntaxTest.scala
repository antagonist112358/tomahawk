package net.mentalarray.doozie.tests

/**
 * Created by kdivincenzo on 9/16/14.
 */

class DSL_SyntaxTest {


    /*
    I.  Operations within the workflow definition block
      val [variable name] = static value { [value or value expression] }

     */

    val testValue = static value ( "The quick brown fox jumped over the lazy dog's back." )

    val deferredValue = deferred value ( testValue + " Turns out the dog was not only lazy, but pretty dumb." )

    val someStaticCmd = static value ( "ls -R /data/raw/yield/" )

    val formattableCmd = static value ( "ls /tmp/%s/*.log" )

    val someBooleanExpr = static value ( false )

    define workflow "import the dataz" as new workflow {

      hdfs run ( static value ( "ls /data" ) )

      // Check
      hdfs run someStaticCmd

      // Check
      hdfs run Command(deferred value ( "ls -R /data/local" ), ignoreErrors = true )

      // Check
      shell run ( formattableCmd format "UXB8" )

      // Check
      shell run Command(
        formattableCmd format "UXB7",
        ignoreErrors = true
      )

      val text = static value "Hello World"

      val text2 = "Hello World"

      val test3 = static value text2

      var def1 = deferred value ("Hello World")

      def1 = def1.mutate(_.toUpperCase)

      val someNewString = deferred value {
        //def1.get() + "," + "Saying Goodbye to this Cruel World!"
      }

      // Check
      val listOfFiles = shell resultOf Command(someStaticCmd) mutate (
          _.split("\\n")
        )

      // Not-Implemented
      /*
        task run {taskType} ( ... )
       */

      // Check
      foreach(listOfFiles) run { a =>
        val formattedCmd = deferred value (formattableCmd format a)

        val output = true
      }


      // Check
      condition when someBooleanExpr then (

      )

      // Check
      condition unless someBooleanExpr then (

      )

      // Check
      condition If someBooleanExpr then (

      )

      // Check
      condition If someBooleanExpr then (

      ) Else (

      )

    }

    /*

    define "import dataz" as new workflow {

      hdfs run hdfsCmdText
      hdfs run Command( ... )

      shell run shellCmdText
      shell run Command( ... )

      task run PigTask( ... )

      foreach [sequence] run (

        val cmdText = static value ( "rm -r -f %s/%s" format(tmpDirectory, "%s" + _ )

        hdfs run Command(cmdText format("WaferET"), ignoreError = true)
        hdfs run Command(cmdText format("itemUnstack"), ignoreError = true)
        hdfs run Command(cmdText format("stepSeqUnstack"), ignoreError = true)
        hdfs run Command(cmdText format("StackedWaferET"), ignoreError = true)

      )

      condition if [boolean] then (

      )

      condition ifElse [boolean] then (

      ) else (

      )

      condition ifElse(
        check = [boolean],
        trueThen = { },
        falseThen = { }
      )

    }

     */

    /*

    run new codeBlock {

    }

    run hdfsCmd "rm -r -f /user/ben/temp"

    resultFrom hdfsCmd "ls /data/tclogs/" { result =>

    }

    run SqoopTask(
      name = "",
      params = {

      }
    )

    resultFrom deferred {

    }

    */

    /*

    val a = run shellCmd "rm" args s"-r -f $path" then
            run shellCmd "del" args "/user/all"


    val b = run hdfsCmd "rm" args "-r -f /user/ben/temp"

    val c = run task "clear old results" as new SqoopTask {
    }

    */

    /*

      step task is new PigTask {
        script = loadFrom /user/ben/script.scala
        parameters = Replace("A" -> "B", "NODE" -> "45nm")

    execute step s"import data on $dateTime" using { step =>
      step task is new SqoopTask {

        username = ""
        query = ParameterizedQuery("SELECT HODOR FROM YRMOM WHERE DOUCHENESS = @level")
        password = ""
      }
      step cleanup is {
      }
    }

    execute step "getMerge and upload" using { step =>
      step task is {
        run hdfsCmd "getmerge" args "/tmp/output/" then
        run hfdsCmd "copyFromLocal" args ... then
        run shellCmd "rm" args "-r -f /tmp/output"
      }
    }

    */

    /*
    define task "execute clean-up pig" using StepBuilder {
      work = new SqoopTask {
      },
      cleanup = {
        run shellCmd "mkdir" args "-p /tmp/storage" then
        run hdfsCmd "getmerge" args "/data/static/somethingSpecial /tmp/storage/data.tsv" then
        run hdfsCmd "copyFromLocal" args "/tmp/storage/data.tsv /data/static/tables/data.tsv" then
        run shellCmd "rm" args "-r -f /tmp/storage"
      },
      faulted = run shellCmd "rm" args "/tmp/intermediate"

    }
    define task "import all yer DAtaZ" using TaskBuilder[SqoopTask] {
      _.required("Some string")
    } thenDo FakeBuilder { _.setting(5) }
    */



}
