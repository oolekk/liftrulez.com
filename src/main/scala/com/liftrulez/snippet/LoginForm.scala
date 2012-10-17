package com.liftrulez.snippet

import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.SHtml
import net.liftweb.http.SessionVar
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Props
import com.liftrulez.model.User

object LoginVar extends SessionVar[Boolean](false)

object LoginForm extends DispatchSnippet with Logger {

  def dispatch = {
    case _ ⇒ render _
  }

  var loginName = ""
  var loginPass = ""

  def render(ns: NodeSeq): NodeSeq = {

    val cssSel = ("*" #> {
      "#loginName *" #> SHtml.text(loginName, loginName = _) &
        "#loginPass *" #> SHtml.password(loginPass, loginPass = _) &
        "#loginSbmt *" #> SHtml.ajaxSubmit("Log In", () ⇒ processLogin(loginName, loginPass))
    })

    SHtml.ajaxForm(cssSel.apply(ns))

  }

  def processLogin(loginName: String, pass: String) {
    if (credentialsOK_?(loginName, pass)) {
      LoginVar(true)
      info("LOGIN ALLOWED FOR: %s".format(loginName))
      S.redirectTo("/manager")
    } else {
      info("LOGIN DENIED FOR: %s".format(loginName))
      S.error("Password or username incorrect.")
    }

  }

  def credentialsOK_?(loginName: String, loginPass: String): Boolean = {
    User.byLogin(loginName).map(_.loginPass.match_?(loginPass)) getOrElse false
  }

}