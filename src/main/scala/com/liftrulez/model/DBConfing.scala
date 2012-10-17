package com.liftrulez.model

import net.liftweb.common._
import net.liftweb.util.ControlHelpers._
import net.liftweb.util.Props

import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.Session
import org.squeryl.adapters._
import org.squeryl.internals.DatabaseAdapter

import java.sql.Connection
import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig

/**
 * DBConfig is a wrapper around settings needed to initialize
 * database connection
 * @param dbType database type to be used - supported values:
 * <b> DB2, Derby, H2, MySQL, MySQLInnoDB, Oracle, PostgreSql </b>
 * @param dbUser database user name
 * @param dbPass database password
 * @param dbUrl jdbc connection string
 * @param dbDriver database driver class name
 * @param dbAdapter database adapter to be used
 */
case class DBConfig(
  val dbType: String,
  val dbUser: String,
  val dbPass: String,
  val dbUrl: String,
  val dbDriver: String,
  val dbAdapter: DatabaseAdapter) {
  /**
   * some DBConfig details
   */
  override def toString = {
    "DBConfig - dbType:%s dbUrl:%s dbDriver:%s dbAdapter:%s".format(
      dbType, dbUrl, dbDriver, dbAdapter.toString)
  }

  /**
   * all the details including dbUser and dbPass
   */
  def fullInfo = {
    "DBConfig - dbType:%s dbUser:%s dbPass:%s dbUrl:%s dbDriver:%s dbAdapter:%s".format(
      dbType, dbUser, dbPass, dbUrl, dbDriver, dbAdapter.toString)
  }

  def getPoolProvider = PoolProvider(this)

}
/**
 * DBConfig companion object helps to create DBConfig class
 * instances based on settings looked up in properties file.
 */
object DBConfig {

  /**
   * Use this factory method to produce Box[DBConfig] from settings
   * looked up in the properties file. If settings are incomplete Failure
   * will be returned.
   * @param dbType denotes database type to be used. It lets us find
   * vendor specific settings in props files, and also to choose the
   * right DatabaseAdapter. Supported values are: <b> DB2, Derby, H2, MySQL,
   * MySQLInnoDB, Oracle, PostgreSql </b>. These exact (case sensitive) names will
   * be used to look up settings. For example, to configure H2
   * database you need to set following properties in the props file:
   * <b> H2.db.user, H2.db.pass, H2.db.url, H2.db.driver </b>
   * To actually use it set <b>use.db=H2</b>
   * Change H2 prefix to a different one to configure other databases.
   * Multiple databases can be configured. <b>use.db</b> property will
   * be looked up to determine which one to use.
   * @return Full[DBSetting] or Failure if some properties are missing or
   * when unrecognized vendor prefix is used.
   */
  def apply(dbType: String): Box[DBConfig] = {

    /* try to get dbType.db.user fall back to db.user or fail if nothing found */
    val dbUser = Props.get(dbType + ".db.user").or(Props.get("db.user")) ?~ (dbType + ".db.user unknown")
    /* try to get dbType.db.pass fall back to db.pass or fail if nothing found */
    val dbPass = Props.get(dbType + ".db.pass").or(Props.get("db.pass")) ?~ (dbType + ".db.pass unknown")
    val dbUrl = Props.get(dbType + ".db.url") ?~ (dbType + ".db.url unknown")
    val dbDriver = Props.get(dbType + ".db.driver") ?~ (dbType + ".db.driver unknown")
    val dbAdapter = dbType match {
      case "DB2"         ⇒ Full(new DB2Adapter)
      case "Derby"       ⇒ Full(new DerbyAdapter)
      case "H2"          ⇒ Full(new H2Adapter)
      case "MySQL"       ⇒ Full(new MySQLAdapter)
      case "MySQLInnoDB" ⇒ Full(new MySQLInnoDBAdapter)
      case "Oracle"      ⇒ Full(new OracleAdapter)
      case "PostgreSql"  ⇒ Full(new PostgreSqlAdapter)
      case x             ⇒ Failure("Unsupported DB type: " + x)
    }

    /* Recursively combine all settings into DBConfig instance.
     * If any of the settings failed return Failure instead of Full[DBConfig]
     */
    val dbConfig = dbUser.flatMap(
      user ⇒ dbPass.flatMap(
        pass ⇒ dbUrl.flatMap(
          url ⇒ dbDriver.flatMap(
            driver ⇒ dbAdapter.map(
              adapter ⇒
                apply(dbType, user, pass, url, driver, adapter))))))

    dbConfig

  }
}

