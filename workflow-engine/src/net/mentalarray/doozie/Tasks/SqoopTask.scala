package net.mentalarray.doozie.Tasks

// TODO - Add parameterized query
class SqoopTask(name: String) extends WorkflowTask(name) {

  private val optMap = collection.mutable.Map[String, String]().withDefaultValue("")

  // Set default values
  numMappers(1)
  fetchSize(1000)

  // Getters
  //====================================================================
  def connect: String = { optMap("connect") }

  def targetDir: String = { optMap("target-dir") }

  def table: String = { optMap("table") }

  def query: String = { optMap("query") }

  def username: String = { optMap("username") }

  def password: String = { optMap("password") }

  def where: String = { optMap("where") }

  def fetchSize: Int = { optMap("fetch-size").toInt }

  def splitBy: String = { optMap("split-by") }

  def numMappers: Int = { optMap("num-mappers").toInt }


  // Setters
  //====================================================================
  protected[workflow] def setProperty[A](key: String, value: A) = {
    optMap(key) = value.toString
  }

  def connect(connStr: String): SqoopTask = {
    optMap.addOrReplace("connect", connStr)
    this
  }

  def targetDir(dir: String): SqoopTask = {
    optMap.addOrReplace("target-dir", dir)
    this
  }

  def table(tableName: String): SqoopTask = {
    optMap.addOrReplace("table", tableName)
    this
  }

  def query(query: String): SqoopTask = {
    optMap.addOrReplace("query", query)
    this
  }

  def username(user: String): SqoopTask = {
    optMap.addOrReplace("username", user)
    this
  }

  def password(pass: String): SqoopTask = {
    optMap.addOrReplace("password", pass)
    this
  }

  def where(whereClause: String): SqoopTask = {
    optMap.addOrReplace("where", whereClause)
    this
  }

  def fetchSize(size: Int): SqoopTask = {
    optMap.addOrReplace("fetch-size", size.toString)
    this
  }

  def splitBy(columnName: String): SqoopTask = {
    optMap.addOrReplace("split-by", columnName)
    this
  }

  def numMappers(count: Int): SqoopTask = {
    optMap.addOrReplace("num-mappers", count.toString)
    this
  }

  // Validation
  //====================================================================
  override def validate = {

    // Verify connectionString is set
    if (connect.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Connection string must be specified.")

    // Verify the targetDir is specified
    if (targetDir.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Target directory must be specified.")

    // Either table name or query must be specified
    if (table.isNullOrWhitespace && query.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Either the select query or the table name must be specified.")

    // Both the table name and query cannot be specified together
    if (!table.isNullOrWhitespace && !query.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Both the table name and query cannot be specified together.")

    // Where is only valid if the table name was specified
    if (!where.isNullOrWhitespace && table.isNullOrWhitespace)
      throw new WorkflowStateException(this, "The 'where' clause is only valid when using the table name specification.")

    // Fetch size must be in the range (1000, 250000) inclusive
    if (fetchSize > 250000 || fetchSize < 1000)
      throw new WorkflowStateException(this, "Fetch size must be in the range (1000, 250000) inclusive.")

    // Num mappers must be in the range of 1 to 100 inclusive.
    if (numMappers < 1 || numMappers > 100)
      throw new WorkflowStateException(this, "Number of mappers must be in the range (1, 100) inclusive.")

    // Split-by must be specified if numMappers is > 1
    if (numMappers > 1 && splitBy.isNullOrWhitespace)
      throw new WorkflowStateException(this, "Split-by must be specified if numMappers is greater than one.")
  }

}

// Companion object
object SqoopTask {

  def apply(fn: SqoopTask => Unit): SqoopTask = {
    val state = new SqoopTask("SqoopTask")
    fn(state)
    state
  }

  def apply(name: String)(cfgFn: SqoopTask => Unit) : SqoopTask = {
    val state = new SqoopTask(name)
    cfgFn(state)
    state
  }

}
