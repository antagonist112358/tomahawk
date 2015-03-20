package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 11/25/14.
 */
// Specification for Templates
class TemplateSpec extends Specification {

  // "Template" generator spec
  "WorkflowTemplates" should {

    "be instantiable from 'Template' generic factory." in {

      // Allow for creation, given a Template type and a Configuration type
      val template = Template[DummyConfiguration, DummyTemplate].create("TestTemplate") {
        new DummyConfiguration(
          text = "Hello World",
          number = 5,
          toggle = true
        )
      }

      template must beAnInstanceOf[WorkflowInstance]
      template must not beNull
    }

  }
  
}


// The dummy configuration
case class DummyConfiguration(text: String, number: Int, toggle: Boolean) extends TemplateConfiguration

// The Test Template
class DummyTemplate(name: String, config: DummyConfiguration) extends WorkflowTemplate(name, config) {

}