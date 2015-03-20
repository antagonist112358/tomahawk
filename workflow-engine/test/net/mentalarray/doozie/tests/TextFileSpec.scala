package net.mentalarray.doozie.tests

import net.mentalarray.doozie.DSL.TextFile
import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 10/6/14.
 */
class TextFileSpec extends Specification {

  "TextFile" should {

    "read files from HDFS" in {

      val path = "/etl/scripts/hive/ETWaferETL.sql"

      val rawText = TextFile.loadFromHdfs(path)

      rawText.isNullOrWhitespace must beFalse
    }

  }

}
