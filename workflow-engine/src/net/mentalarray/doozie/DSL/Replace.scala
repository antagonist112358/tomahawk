package net.mentalarray.doozie.DSL

/**
 * Created by kdivincenzo on 9/29/14.
 */
object Replace {

  def apply(replacements: (String,Any)*) = {
    val instance = ReplacementParameters()
    replacements.foreach(r => instance.replace(r))
    instance
  }

}
