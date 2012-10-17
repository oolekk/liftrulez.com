package com.liftrulez.model

import org.squeryl.Schema
import net.liftweb.squerylrecord.RecordTypeMode._

import net.liftweb.common.Loggable

/**
 * Defines database tables and constraints for liftrulez.com webapp.
 */
object DBSchema extends Schema with Loggable {

  val user = table[User]("user")
  on(user)(u ⇒ declare(
    u.loginName defineAs (unique)))

}
