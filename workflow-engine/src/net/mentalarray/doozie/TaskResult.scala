package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 10/22/14.
 */

object ValueProxy {

  // Child class
  class TaskResult[T](valueGenerator: Unit => T) {
    private var _valueGen: Unit => T = valueGenerator

    def mutate[V](fn: T => V): TaskResult[V] = new TaskResult[V](_ => fn(_valueGen()))

    protected[ValueProxy] def get: T = _valueGen()

    protected def set(valExpr: Unit => T) = _valueGen = valExpr
  }

  object TaskResult {
    def apply[T](valueGenerator: Unit => T) = new TaskResult(valueGenerator)
  }

  // Internal Helper class for deferred value extraction
  protected[workflow] class TaskResultReader[T](input: TaskResult[T]) {
    def read = input.get
  }

  protected[workflow] object TaskResultReader {
    def apply[T](result: TaskResult[T]) : TaskResultReader[T] = new TaskResultReader(result)
  }
}