package com.liftrulez.model

import net.liftweb.common._
import net.liftweb.util.Props
import net.liftweb.util.ControlHelpers._
import net.liftweb.http.LiftRules
import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.Session
import org.squeryl.internals.DatabaseAdapter
import java.sql.Connection
import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *  database connection pooling provider - we are using BoneCP
 *  DBConfig and SquerylRecord session
 *  @param dbConfig  database configuration holder
 *  @param minConPerPartition  will be passed to BoneCP
 *  @param maxConPerPartition  will be passed to BoneCP
 *  @param partitionCount  will be passed to BoneCP
 */
case class PoolProvider(
  val dbConfig: DBConfig,
  minConPerPartition: Int = 4,
  maxConPerPartition: Int = 8,
  partitionCount: Int = 1) {

  val log = LoggerFactory.getLogger(classOf[PoolProvider])
  val sqlLog = LoggerFactory.getLogger(
    classOf[PoolProvider].getName + ".sqlLog")

  private val pool = initPool()

  def shutdownPool() = pool.shutdown()

  LiftRules.unloadHooks.append(() ⇒ {
    log.info("BoneCP: about to close connection pool.")
    shutdownPool();
    log.info("BoneCP: connection pool is now closed.")
  })

  def getConnection = pool.getConnection

  private def initPool(): BoneCP = {
    log.info("BoneCP: about to initialize connecnion pool.")

    // create a new configuration object	
    val config = new BoneCPConfig

    log.info("BoneCP: partitionCount:%s minConPerPartition:%s maxConPerPartition:%s".format(
      partitionCount, minConPerPartition, maxConPerPartition))

    config.setPartitionCount(partitionCount)
    config.setMinConnectionsPerPartition(minConPerPartition)
    config.setMaxConnectionsPerPartition(maxConPerPartition)

    // show dbConfig beign used
    log.info("" + dbConfig)

    // load the DB driver class
    Class.forName(dbConfig.dbDriver)
    // set the JDBC url
    config.setJdbcUrl(dbConfig.dbUrl)
    // set the username
    config.setUsername(dbConfig.dbUser)
    // set the password
    config.setPassword(dbConfig.dbPass)

    // setup the connection pool
    val pool = new BoneCP(config)

    log.info("BoneCP: connection pool is now initialized.")

    pool
  }

  private def mkSquerylSession(): Session = {
    val session = Session.create(getConnection, dbConfig.dbAdapter)
    if (Props.getBool("log.dbsession", false)) {
      session.setLogger(statement ⇒ sqlLog.debug(statement))
    }
    session
  }

  def initSquerylRecordSession() {
    log.info("Initializing squeryl-record session ...")
    SquerylRecord.initWithSquerylSession(mkSquerylSession())
    log.info("squeryl-record session initialized.")
  }

}

