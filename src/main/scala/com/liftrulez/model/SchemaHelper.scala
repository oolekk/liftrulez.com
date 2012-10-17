package com.liftrulez.model

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props
import net.liftweb.util.LoanWrapper
import net.liftweb.http.S
import net.liftweb.http.LiftRules
import net.liftweb.squerylrecord.RecordTypeMode._

/*
 *  Helper object that initiates the db connection and creates the schema 
 *  Do not use this (createShema) approach in production  
 */
object SchemaHelper extends Logger {

  /**
   * database initialization
   * @param dbType database type to be used. Supported values
   * are <b> DB2, Derby, H2, MySQL, MySQLInnoDB, Oracle, PostgreSql </b>
   * @param schemify_? should we recreate database schema? If some tables
   * already exist, they will be dropped.
   * @param wrapAround_? when set to true LoanWrapper will be used to wrap requests
   * inside transaction.
   */
  def initDB(
    dbType: String,
    schemify_? : Boolean = false,
    wrapAround_? : Boolean = true): Unit = {

    info("About to init db dbType:%s schemify_?:%s, wrapAround_?:%s".format(
      dbType, schemify_?, wrapAround_?))

    /* Create boxed DBConfig based on settings from props file */
    val configBox = DBConfig(dbType)
    if (configBox.isEmpty) error("Failed to create DBConfig: " + configBox)
    else {
      configBox.map(_.getPoolProvider.initSquerylRecordSession)

      if (wrapAround_?)
        info("About to wrap around")
      /* Wrap DB transaction wrap around whole HTTP request */
      S.addAround(new LoanWrapper {
        override def apply[T](f: ⇒ T): T = { inTransaction { f } }
      })

      /* Shall we drop & recreate schema? */
      if (schemify_?) {
        info("About to schemify")
        SchemaHelper.dropAndCreateSchema
      } else touchDB()

      if (Props.mode == Props.RunModes.Development && dbType == "H2") {
        info("Setting up H2 console under /h2 ")
        LiftRules.liftRequest.append({
          case r if (r.path.partPath match {
            case "h2" :: _ ⇒ true
            case _         ⇒ false
          }) ⇒ false
        })

      }
    }

  }

  /**
   *  just touch the database
   */
  def touchDB() { transaction {} }

  /**
   * drop existing database schema and create a new one
   */
  def dropAndCreateSchema() {
    transaction {
      info("About to drop and create db schema ...")
      try {
        /* Print database definition statements */
        DBSchema.printDdl
        /* Drop existing schema if present */
        DBSchema.drop
        /* Create db schema by executing database definition statements */
        DBSchema.create
        /* Fill the db with initial data */
        DemoData.createDemoData
      } catch {
        case e ⇒ {
          error("... failed to create schema")
          e.printStackTrace()
          throw e
        }
      }
    }
    info("... schema created")
  }

}

