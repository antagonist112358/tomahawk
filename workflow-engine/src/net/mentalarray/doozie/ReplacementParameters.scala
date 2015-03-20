package net.mentalarray.doozie

import scala.collection.mutable
import scala.util.matching.Regex


/**
 * Created by kdivincenzo on 9/26/14.
 */

/**
 * Companion object for ReplacementParameters class.
 */
object ReplacementParameters {
  final val defaultTokenRegex = """\$(\w+)""".r

  def apply() : ReplacementParameters = new ReplacementParameters(defaultTokenRegex)
}

class ReplacementParameters(tokenRegex: Regex) {

  private final val keyFormatRegex = """(\w+)""".r
  private val replacements = mutable.Map.empty[String, String]

  def this(tokenExpr: String) = this(tokenExpr.r)

  def replace(map: Tuple2[String, Any]) = {

    // Clean up the input string
    val key = keyFormatRegex.findFirstIn(map._1) match {
      case Some(keyName) => keyName.toUpperCase
      case None => throw new IllegalArgumentException("Replacement key: %s is invalid." format map._1)
    }

    // Get the value of the key
    val value = map._2.toString

    // Replace or add the replacement key -> value
    replacements.addOrReplace(key, value)

    // For fluent
    this
  }

  def formatText(text: String) : String = {
    // Find and iterate over all parameter matches in the text
    tokenRegex.replaceAllIn(text, m => {
      // Key text
      val keyText = m.group(1).toUpperCase
      // Check for a replacement
      if (replacements.keySet.exists(_ == keyText)) {
        replacements(keyText)
      } else {
        // No replacement, so give the token back
        m.toString.replace("$", "\\$")
      }
    })
  }

}
