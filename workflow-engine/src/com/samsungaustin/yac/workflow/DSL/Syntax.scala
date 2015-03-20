/**
 * Created by kdivincenzo on 11/21/14.
 */

/*** Commenting due to persistent warning during compile ***/

//package com.samsungaustin.yac.workflow.DSL

//import com.samsungaustin.yac.workflow.Builders.WorkflowBuilder
//import com.samsungaustin.yac.workflow.ValueProxy.{TaskResult, TaskResultReader}
//import com.samsungaustin.yac.workflow.WorkflowInstance

/*
object Syntax {  

  object static {
    def value[U](valExpr: U) : TaskResult[U] = TaskResult(_ => valExpr)
  }

  object deferred {
    def value[U](valExpr: => U) : TaskResult[U] = TaskResult(_ => valExpr)
  }

  object define {
    
    def workflow(name: String) : AsExtender[workflow, WorkflowInstance] = new WorkflowAsExtender(name)
    
  } 

  trait ThenStarter {
    def then[That](exprBlock: => That) : Unit
  }

  trait AsExtender[-TIn, +TOut] {
    def as(instance: => TIn) : TOut
  }

  trait ElseExtender {
    def Else[That](codeBlock : => That) : Unit     
  }
  
  // Trait for defining the current "starter objects" when defining the workflow
  trait WorkflowContext {

    // Implicit conversions
    implicit def taskResultConverter[T](result: TaskResult[T]) : T = {
      new TaskResultReader[T](result).read
    }

    implicit def iterableToSeq[T](input: TaskResult[Iterable[T]]) : TaskResult[Seq[T]] = input.mutate(_.toSeq)

    implicit def arrayToSeq[T](input: TaskResult[Array[T]]) : TaskResult[Seq[T]] = input.mutate(_.toSeq)
    
    // HDFS actions
    object hdfs {

      // run operation
      def run(cmd : AbstractCommand)(implicit builder: WorkflowBuilder) = {
        
      }

      // Results from
      def resultOf(cmd: Command)(implicit builder: WorkflowBuilder) : TaskResult[String] = ???
    }

    // Shell actions
    object shell {

      // run operation
      def run(cmd : AbstractCommand)(implicit builder: WorkflowBuilder) = {

      }

      // Results from
      def resultOf(cmd: Command)(implicit builder: WorkflowBuilder) : TaskResult[String] = ???
    }

    // Task actions
    object task {

    }

  }

  // Trait for defining the various commands/operations which can be used when defining a workflow
  trait WorkflowOperations {

    // Foreach operation 
    def foreach[A](resultSeq: TaskResult[Seq[A]]) = new Iterator(resultSeq)

    // Condition operations
    object condition {

      // When (boolean) then do ...
      def when(defBool: TaskResult[Boolean]) : ThenStarter = new WhenStarter(defBool)

      // Unless (boolean) then do ...
      def unless(defBool: TaskResult[Boolean]) : ThenStarter = new UnlessStarter(defBool)

      // IfElse
      def If(defBool: TaskResult[Boolean]) : IfElseStarter = new IfElseStarter(defBool)
    }

  }

  sealed class Iterator[+A](targetSequence: TaskResult[Seq[A]]) {
    
    def run[That](codeBlock: A => That) : Unit = ???
    
  }
  
  sealed class WhenStarter(defBool: TaskResult[Boolean]) extends ThenStarter {
    
    def then[That](exprBlock: => That) : Unit = {
      
    }
    
  }

  sealed class UnlessStarter(defBool: TaskResult[Boolean]) extends ThenStarter {

    def then[That](exprBlock: => That) : Unit = {

    }

  }

  sealed class ConditionalElseExtender extends ElseExtender {

    def Else[That](codeBlock : => That) : Unit = {
      
    }
    
  }
  
  sealed class IfElseStarter(defBool : TaskResult[Boolean]) {
    
    def then[That](exprBlock: => That) : ElseExtender = new ConditionalElseExtender
    
  }
  
  sealed class WorkflowAsExtender(wfName: String) extends AsExtender[workflow, WorkflowInstance] {
    override def as(instance: => workflow): WorkflowInstance = {
      // Get the new workflow class
      val wfClassInstance = instance
      // Set the workflow name
      wfClassInstance.assignName(wfName)
      // Build the instance
      wfClassInstance.build
    }
  }
  
  abstract class workflow extends WorkflowContext with WorkflowOperations {

    // Internally - we use a WorkflowBuilder to create the workflow
    implicit val builder = new WorkflowBuilder

    // Keeps track of the workflow name
    private var wfName: String = "{Unnamed Workflow}"
    
    // Used to assign the workflow name
    protected[Syntax] def assignName(name: String) = { wfName = name }
    
    // Used to build the workflow using the internal builder
    protected[Syntax] def build = builder.build
  }

}
*/