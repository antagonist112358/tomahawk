package net.mentalarray.doozie.DatabaseLibrary

/**
 * Created by bgilcrease on 10/3/14.
 */

object DBConnectionString {

  trait PostgresqlConnectionString { self: DBConnectionString =>

    protected val _host: String
    protected val _port: String
    protected val _user: String
    protected val _password: String
    protected val _database: String

    def connectionString: String = {
      "jdbc:postgresql://%s:%s/%s?user=%s&password=%s" format(_host, _port, _database, user, password)
    }

  }

  trait HiveConnectionString { self: DBConnectionString =>

    protected val _host: String
    protected val _port: String
    protected val _user: String
    protected val _password: String

    def connectionString: String = {
      "jdbc:hive2://%s:%s/" format(_host, _port)
    }

  }

  trait DB2ConnectionString { self: DBConnectionString =>
    protected val _host: String
    protected val _port: String
    protected val _database: String
    protected val _options: String

    def connectionString: String = {
      "jdbc:db2://%s:%s/%s:%s" format (_host, _port, _database, _options)
    }
  }

  trait OracleConnectionString { self: DBConnectionString =>
    protected val _host: String
    protected val _port: String
    protected val _database: String

    def connectionString: String = {
      "jdbc:oracle:thin:@%s:%s/%s" format (_host, _port, _database)
    }
  }

  trait MSSQLConnectionString { self: DBConnectionString =>
    protected val _host: String
    protected val _port: String
    protected val _database: String

    def connectionString: String = {
      "jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;" format (_host, _port, _database, user, password)
    }
  }

}

trait DBConnectionString {

  protected val _host : String
  protected val _port: String
  protected val _user: String
  protected val _password: String

  def connectionString: String
  def user: String = EncryptDecrypt().decrypt(_user)
  def password: String = EncryptDecrypt().decrypt(_password)

}


