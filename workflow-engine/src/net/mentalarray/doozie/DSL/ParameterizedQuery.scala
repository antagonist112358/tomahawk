package net.mentalarray.doozie.DSL

/**
 * Created by kdivincenzo on 9/29/14.
 */
object ParameterizedQuery {

  def apply(query: String, replacements: ReplacementParameters) = {
    replacements.formatText(query)
  }

}
