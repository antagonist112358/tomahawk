package net.mentalarray.doozie.Internal

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.yarn.api.records.{FinalApplicationStatus, YarnApplicationState}
import org.apache.spark.SparkConf
import org.apache.spark.deploy.yarn.{Client => SparkClient, ClientArguments}

import scala.collection.mutable
import scala.xml.XML


/**
 * Created by bgilcrease on 11/4/14.
 * inspired by sequenceiq spark-submit-runner with code from spark.deploy.yarn.Client
 */

object SparkRunner extends Logging {

  private var arguments: mutable.MutableList[String] = mutable.MutableList[String]()

  private def getPropXmlAsMap(filePath: String): Map[String, String] = {
    Map[String, String]() ++ (for (yarnProp <- (XML.loadFile(filePath) \\ "property"))
    yield (yarnProp \ "name").text -> (yarnProp \ "value").text).toMap
  }

  private def fillProperties(config: Configuration, props: Map[String, String]) {
    for ((key, value) <- props) config.set(key, value)
  }

  def run(): Boolean = {
    val config = new Configuration()

    fillProperties(config, getPropXmlAsMap("/etc/gphd/hadoop/conf/core-site.xml"))
    fillProperties(config, getPropXmlAsMap("/etc/gphd/hadoop/conf/yarn-site.xml"))

    System.setProperty("SPARK_YARN_MODE", "true")
    val sparkConf = new SparkConf()
    val cArgs = new ClientArguments(arguments.toArray, sparkConf)
    val client = new SparkClient(cArgs, config, sparkConf)
    val appId = client.runApp
    val interval = sparkConf.getLong("spark.yarn.report.interval", 10000)

    while (true) {
      Thread.sleep(interval)
      val report = client.getApplicationReport(appId)

      Log debug ("Application report from ResourceManager: \n" +
        "\t application identifier: " + appId.toString() + "\n" +
        "\t appId: " + appId.getId() + "\n" +
        "\t clientToAMToken: " + report.getClientToAMToken() + "\n" +
        "\t appDiagnostics: " + report.getDiagnostics() + "\n" +
        "\t appMasterHost: " + report.getHost() + "\n" +
        "\t appQueue: " + report.getQueue() + "\n" +
        "\t appMasterRpcPort: " + report.getRpcPort() + "\n" +
        "\t appStartTime: " + report.getStartTime() + "\n" +
        "\t yarnAppState: " + report.getYarnApplicationState() + "\n" +
        "\t distributedFinalState: " + report.getFinalApplicationStatus() + "\n" +
        "\t appTrackingUrl: " + report.getTrackingUrl() + "\n" +
        "\t appUser: " + report.getUser()
      )

      val state = report.getYarnApplicationState()
      if (state == YarnApplicationState.FINISHED ) {
        val status = report.getFinalApplicationStatus()
        if ( status == FinalApplicationStatus.SUCCEEDED ) {
          return true
        } else {
          return false
        }
      } else if ( state == YarnApplicationState.FAILED || state == YarnApplicationState.KILLED) {
        return false
      }
    }
    false
  }

  private case class sparkArgument(argument: String, value: String)

  def addArgs(args: Array[String]) {
    arguments = arguments ++ args
  }

  def addArgument(argument: String, value: String): Unit = {
    arguments += s"--$argument"
    arguments += value
  }

}
