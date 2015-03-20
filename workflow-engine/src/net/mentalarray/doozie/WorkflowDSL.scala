package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/24/14.
 */

/*
Run vs. Define vs. Execute for tasks:
  Run     - Run allows the task to be specified (or takes an existing Task), then adds it to the (scoped) workflow.
  Define  - Allows a workflow to be defined and assigned to a variable (or passed to a function) but *does not* add it to the workflow    
  
  Task Types: Sqoop, DBQuery, Pig, Scalding, Hive
  Builders: TaskBuilder, StepBuilder
  Command Types: shellCmd, hdfsCmd
  
   
  Syntax:
  ===============================================================================================

  Defining a workflow:  
  1)  define workflow "Workflow name" as { ... }

  Defining a task:   
  1)  'Symbol is new {TaskType} { }
        
  Defining a command:  
  1)  'Symbol is {CommandType} "Command to execute" [args "Arguments to command"] 

  Defining a step:
  1)  'Symbol is step "Name of Step" 
       
  Defining a sequence of tasks:
  1)  [val sequence =] execute 'Symbol1 then 'Symbol2 then 'Symbol3
  2)  execute new {TaskType} { ... } then new {TaskType} { ... }
                  
  Running a task:
  1)  run task 'Symbol
  2)  run task named "Name of Task" is new {TaskType} { ... }
  
  
    
*/


object WorkflowDSL {

  // Assigner class
  sealed class Assigner {

    // Assign a static value
    def value[TVal](value: TVal) = {
      new ValueAssigner[TVal](value)
    }

    // Assign the result of a task
    def resultFrom = ???

  }

  // All possible base/root level syntax ops
  trait WorkflowActions {

    private val _assigner = new Assigner

    // Value to alias assignment
    def assign = _assigner

    // Run a workflow task
    def run

  }
  
  // Implementing class is used to define things   
  trait Definer[-TIn, +TOut] {
    def as(expr: => TIn) : TOut
    //def using[B <: Builder[TIn]](builder: => B) : TOut
  }
  
  // Implementing class is used to run/start things
  trait Starter[-A] {
    def as(item: => A) : Unit
  }

  trait ValueAssignment {
    def to(symAlias : Symbol) : WorkflowActions
  }

  // Run
  object run {
    // Task
    def task(name: String) = {
      new TaskStarter(name)
    }
    // Bash
    def shellCmd(cmdStr: String) = {
      new ShellCommandBuilder(cmdStr)
    }
    // Hdfs
    def hdfsCmd(cmdName: String) = {
      new HdfsCommandBuilder(cmdName)
    }
  }
  
  // Define
  object define {

    // workflow
    def workflow(name: String) = {
     new WorkflowSpecifier(name)
    }

    def task(name: String) = {
      new TaskDefiner(name)
    }
  }

  // Assign
  object assign {

    // value
    def value[TVal](value: TVal) = {
      new ValueAssigner[TVal](value)
    }

  }

  // Specifier classes
  // =========================================
  
  class WorkflowSpecifier(name: String) {

    implicit val wfBuilder: WorkflowBuilder = new WorkflowBuilder(name)

    def as[Any](expr: => Any) : WorkflowBuilder = wfBuilder

  }


  // Definer classes
  // =========================================

  class TaskDefiner(name: String) extends Definer[WorkflowTask, WorkflowTask] {

    implicit val taskName: String = name

    override def as(expr: => WorkflowTask): WorkflowTask = expr

    //override def using[B <: Builder[WorkflowTask]](builder: => B): WorkflowTask = builder.build
  }

  class ValueAssigner[TVal](value: TVal) extends ValueAssignment {
    def to(symAlias: Symbol) = ???
  }

  // Builder Classes
  // =========================================
  
  class ShellCommandBuilder(cmd: String) {

    def args(arguments: String*): BashTask = {

      new BashTask("Shell Command: %s %s".format(cmd, arguments.mkString(" ")))

    }

  }

  class HdfsCommandBuilder(cmdName: String) {

    def args(arguments: String*): HdfsTask = ???

  }


  // Starter classes
  // =========================================

  class TaskStarter(name: String) {

    implicit val taskName: String = name

    def as[T <: WorkflowTask](task: => T) : Unit = {

      task

    }

  }

  // Helper methods
  // =========================================
  def is[T](gen: T) = gen
  
  class DummyClass {
    
    private var _errorTask : WorkflowTask = null
    
    def cleanup[T <: WorkflowTask](task: => T): Unit = _errorTask = task
    
  }
  
  // Helper classes
  // =========================================
  
  class TaskWorkflowExtensions[A <: WorkflowTask](task: A) {

  }
  
  // Implicit casts
  implicit def addExtensionsToTask(task: WorkflowTask) : TaskWorkflowExtensions[_] = new TaskWorkflowExtensions(task)
}