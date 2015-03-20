package net.mentalarray.doozie.Builders

/**
 * Created by kdivincenzo on 11/25/14.
 */

trait TemplateConfiguration {

}

abstract class WorkflowTemplate[T <: TemplateConfiguration](name: String, config: => T) extends WorkflowBuilder(name) {


}

class TemplateFactory[C <: TemplateConfiguration, T <: WorkflowTemplate[C]](implicit m : Manifest[T]) {
  def apply(name : String, config: C)(implicit c: Manifest[C]) = {
    val configClass = c.erasure
    val template = m.erasure.getConstructor(classOf[String], configClass).newInstance(name, config)
    template.asInstanceOf[T]
  }
}

class Template[C <: TemplateConfiguration, T <: WorkflowTemplate[C]](implicit m: Manifest[T]) {

  def create(name: String)(config: => C)(implicit c : Manifest[C]) : WorkflowBuilder = {

    val factory = new TemplateFactory[C, T]
    factory(name, config)
  }

}

object Template {

  def apply[C <: TemplateConfiguration : Manifest, T <: WorkflowTemplate[C] : Manifest]() = {
    new Template[C,T]
  }

}