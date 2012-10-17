package com.liftrulez.model

import net.liftweb.util.Props
import net.liftweb.common.Logger
import net.liftweb.squerylrecord.RecordTypeMode._

/**
 * Adds initial data to the database. If db is created by SchemaHelper createDemoData
 * will be run right after.
 */
object DemoData extends Logger {

  /**
   * This function will be executed after the db schema is created by SchemaHelper.
   * Put your data initialization logic in here.
   */
  def createDemoData = {

    info("Create demo data")
    /* Attempt to read credentials from props file */
    val adminLoginName = Props.get("site.admin.login") getOrElse "nimda"
    val adminLoginPass = Props.get("site.admin.pass") getOrElse "drowssap"

    val adminUser = User.createRecord loginName adminLoginName loginPass adminLoginPass

    inTransaction { User.table.insert(adminUser) }

  }

}
