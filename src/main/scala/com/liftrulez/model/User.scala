package com.liftrulez.model

import net.liftweb.record.field._
import net.liftweb.record.Record
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers._
import org.squeryl.annotations.Column

class User extends Record[User] with KeyedRecord[Long] {

  def meta = User

  @Column(name = "id")
  val idField = new LongField(User.this)

  val loginName = new StringField(User.this, "")
  val loginPass = new PasswordField(User.this)

}

object User extends User with MetaRecord[User] {

  def table = DBSchema.user

  def idFromString(in: String) = tryo { in.toLong }

  def byId(in: Long): Option[User] = inTransaction {
    table.lookup(in)
  }

  def byId(in: String): Option[User] =
    idFromString(in).flatMap(byId(_))

  def byLogin(loginName: String): Option[User] = inTransaction {
    from(table)(user ⇒ where(user.loginName === loginName) select (user)).headOption
  }
}
