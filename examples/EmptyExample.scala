/**
 * Created by kdivincenzo on 12/10/14.
 */

import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.DSL.Extensions._

class EmptyWorkflow extends WorkflowBuilder("No-op Workflow") with Logging {

  appendStep {
    """echo "Hello World" """.shellCmd
  }

}


new EmptyWorkflow()