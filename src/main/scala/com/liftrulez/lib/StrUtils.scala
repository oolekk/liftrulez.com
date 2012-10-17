package com.liftrulez.lib

import net.liftweb.common._

object StrUtils {

  def afterLast(str: String, mark: String): String = {
    val markAt = str.lastIndexOf(mark) + mark.length
    str.substring(markAt)
  }

  def afterLastDot(str: String): String =
    afterLast(str, ".")

  def countRE(in: String, re: String): Int =
    (re.r).findAllIn(in).length

  def clearLeft(in: String, sep: String): String = {
    if (in.startsWith(sep)) in.substring(sep.length)
    else in
  }

  def clearRight(in: String, sep: String): String = {
    if (in.endsWith(sep)) in.substring(0, in.length - sep.length)
    else in
  }

  def fillLeft(in: String, sep: String): String = {
    if (in.startsWith(sep)) in
    else sep + in
  }

  def fillRight(in: String, sep: String): String = {
    if (in.endsWith(sep)) in
    else in + sep
  }

}