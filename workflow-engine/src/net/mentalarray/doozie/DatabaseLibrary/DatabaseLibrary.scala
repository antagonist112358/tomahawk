package net.mentalarray.doozie.DatabaseLibrary

import java.sql.DriverManager

import net.mentalarray.doozie.DBStore.JDBCConnection
import org.apache.hive.jdbc.HiveDriver

/**
 * Created by bgilcrease on 12/29/14.
 */
sealed trait DatabaseLibrary extends JDBCConnection { }

object DatabaseLibrary {

  case object MOSDr extends DatabaseLibrary {
    override protected val _connectionInfo = new DBConnectionString with OracleConnectionString {
      protected val _host = "???"
      protected val _port = "1521"
      protected val _database = "table"
      protected val _user = "uEjMmeape98="
      protected val _password = "nVPpFH7sArOmIU2J2mjXgA=="
    }

    protected def registerDriver = {
      Class.forName("oracle.jdbc.OracleDriver")
    }

  }

  case object HiveMetastore extends DatabaseLibrary {

    override protected val _connectionInfo = new DBConnectionString with PostgresqlConnectionString {
      protected val _host = "???"
      protected val _port = "10432"
      protected val _database = "table"
      protected val _user = "WVphzwwMnHM="
      protected val _password = "bhJ8X/9F9KflylirEMTivA=="
    }

    protected def registerDriver = {
      Class.forName("org.postgresql.Driver")
    }
  }

  case object YMSdev extends DatabaseLibrary with DB2SchemaBuilder {

    override protected val _connectionInfo =  new DBConnectionString with DB2ConnectionString {
      protected val _host = "???"
      protected val _port = "60000"
      protected val _database = "table"
      protected val _options = "currentSchema=BALL;"
      protected val _user = "iOxr83gQvhY="
      protected val _password = "amjDB0E4Fu8="
    }

    protected def registerDriver = {
      Class.forName("com.ibm.db2.jcc.DB2Driver")
    }

  }

  case object YMS extends DatabaseLibrary with DB2SchemaBuilder {

    override protected val _connectionInfo = new DBConnectionString with DB2ConnectionString {
      protected val _host = "???"
      protected val _port = "60000"
      protected val _database = "table"
      protected val _options = "currentSchema=SOR;"
      protected val _user = "txTQuXSuBVY="
      protected val _password = "Tv84tG955J/k1h5pAd+gLg=="
    }

    protected def registerDriver = {
      Class.forName("com.ibm.db2.jcc.DB2Driver")
    }
  }

  case object HiveDB extends DatabaseLibrary {
    override protected val _connectionInfo = new DBConnectionString with HiveConnectionString {
      protected val _host = "???"
      protected val _port = "10000"
      protected val _user = "Jf3i3iQL5Ag="
      protected val _password = "hudVCS5W8ks="
    }

    protected def registerDriver = {
      DriverManager.registerDriver(new HiveDriver())
    }
  }

  case object HiveDBDev extends DatabaseLibrary {
    override protected val _connectionInfo = new DBConnectionString with HiveConnectionString {
      protected val _host = "???"
      protected val _port = "10000"
      protected val _user = "Jf3i3iQL5Ag="
      protected val _password = "hudVCS5W8ks="
    }

    protected def registerDriver = {
      DriverManager.registerDriver(new HiveDriver())
    }
  }

  case object TTTMDev extends DatabaseLibrary {

    override protected val _connectionInfo = new DBConnectionString with MSSQLConnectionString {
      protected val _host = "???"
      protected val _port = "1433"
      protected val _database = "table"
      protected val _user = "X+jDj1kOkwR5qr9tnDnfDw=="
      protected val _password = "XIWxMhUQoUoyc9rFzENfng=="
    }

    protected def registerDriver = {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
    }
  }

  case object TTTM extends DatabaseLibrary {

    override protected val _connectionInfo = new DBConnectionString with MSSQLConnectionString {
      protected val _host = "???"
      protected val _port = "1433"
      protected val _database = "table"
      protected val _user = "X+jDj1kOkwR5qr9tnDnfDw=="
      protected val _password = "XIWxMhUQoUoyc9rFzENfng=="
    }

    protected def registerDriver = {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
    }
  }
}
