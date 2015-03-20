package net.mentalarray.doozie.tests

import org.specs2.control.Debug
import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/26/14.
 */
class ReplacementsSpec extends Specification with Debug {

  "ReplacementParameters" should {

    "replace a singe token '$name' in a string" in {
      val sourceStr = "$name"
      val expectedStr = "hello world"
      val replacer = ReplacementParameters()
      replacer.replace("name" -> "hello world")

      replacer.formatText(sourceStr) must beEqualTo(expectedStr)
    }

    "replaces any case token in a string" in {
      val sourceStr = "$word $Word $wOrD"
      val expectedStr = "toYourMother toYourMother toYourMother"
      val replacer = ReplacementParameters()
      replacer.replace("$word" -> "toYourMother")

      replacer.formatText(sourceStr) must beEqualTo(expectedStr)

    }

    "be able to replace multiple tokens in a string" in {
      val server = "fileserver1"; val port = 421; val path = "uploaded"
      val sourceStr = "ftp://$server:$port/data/$dataPath/filetarget.txt"
      val expectedStr = s"ftp://$server:$port/data/$path/filetarget.txt"
      val replacer = ReplacementParameters()
      replacer
        .replace("server" -> server)
        .replace("port" -> port)
        .replace("dataPath" -> path)

      replacer.formatText(sourceStr) must beEqualTo(expectedStr)

    }

    "allow flexible token formats for replacment" in {
      val query =     "SELECT [Username], [AccountStatus] FROM Users WHERE [Password] = @password"
      val expected =  "SELECT [Username], [AccountStatus] FROM Users WHERE [Password] = myPassword"
      val replacer = new ReplacementParameters("""\@(\w+)""")
      replacer.replace("password" -> "myPassword")

      replacer formatText query must beEqualTo(expected)
    }

    "does not replace or error on tokens without replacements" in {
      val formatted = "$first $second $ThIrd_1"
      val expected  = "Value1 $second Value3"
      val replacer = ReplacementParameters()
      replacer
        .replace("first" -> "Value1")
        .replace("$THIRd_1" -> "Value3")

      replacer formatText formatted must beEqualTo(expected)
    }

  }

}
