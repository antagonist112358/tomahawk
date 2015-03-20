package net.mentalarray.doozie.Tasks


/**
 * Created by bwilson on 12/16/14.
 */
class FileBuilderTask(Jobname: String) extends WorkflowTask(Jobname) {

  // Set defaults and accessors for the Builder Runner.
  private var _srcSys: String = "hdfs"
  private var _destSys: String = "hdfs"
  private var _inPath: String = ""
  private var _outPath: String = ""
  private var _srcCheckDel: Boolean = false
  private var _stringAdd: String = ""

  // Getters

  def inPath: String = {
    _inPath
  }

  def outPath: String = {
    _outPath
  }

  def srcCheckDel: Boolean = {
    _srcCheckDel
  }

  def stringAdd: String = {
    _stringAdd
  }

  def srcSys: String = {
    _srcSys
  }

  def destSys: String = {
    _destSys
  }

  // Setters

  def inPath(inPath: => String): FileBuilderTask = {
    _inPath = inPath
    this
  }

  def outPath(outPath: => String): FileBuilderTask = {
    _outPath = outPath
    this
  }

  def srcCheckDel(delSrc: => Boolean): FileBuilderTask = {
    _srcCheckDel = srcCheckDel
    this
  }

  def stringAdd(stringAdd: => String): FileBuilderTask = {
    _stringAdd = stringAdd
    this
  }

  def srcSys(srcSys: => String): FileBuilderTask = {
    _srcSys = srcSys
    this
  }

  def destSys(destSys: => String): FileBuilderTask = {
    _destSys = destSys
    this
  }

  // Perform validation of all input parameters.
  override def validate = {

    //Verify that input path has been supplied.
    if (inPath.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Input directory path must be specified.")

    //Verify that the target output path has been supplied.
    if (outPath.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Output directory path must be specified.")

  }

}

object FileBuilderTask {
  def apply(Jobname: String)(cfgFn: FileBuilderTask => Unit): FileBuilderTask = {
    val state = new FileBuilderTask(Jobname)
    cfgFn(state)
    state
  }

}