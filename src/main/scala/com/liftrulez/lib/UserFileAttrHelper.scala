package com.liftrulez.lib

import net.liftweb.common._
import net.liftweb.util.ControlHelpers._
import java.nio.file.Files
import java.nio.file.Path
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.attribute.UserDefinedFileAttributeView

/**
 * Provides methods for storing and retrieving custom path attributes.
 * We may thus define additional path system attributes, to create and track our
 * custom path metadata. This can be very useful to store metadata such as sha or
 * md5 hash, mime type or perhaps image dimensions info.
 * To enable this feature on Linux server user_xattr mount option should be set.
 */
object CustomFileAttrHelper {

  /**
   * @param path denotes path for which we want to acquire  view
   * @return user defined attribute view for a given path
   */
  def getUserAttrView(path: Path) =
    Files.getFileAttributeView(path, classOf[UserDefinedFileAttributeView])

  /**
   * Attempt to read user defined String attribute.
   * @param path denotes file or dir to be queried
   * @param name attribute's identifier
   * @return attributes value as Box[String]
   */
  def readUserStrAttr(path: Path, name: String): Box[String] = tryo {
    val view = getUserAttrView(path)
    val buff = ByteBuffer.allocate(view.size(name))
    view.read(name, buff)
    buff.flip.asInstanceOf[ByteBuffer]
    Charset.defaultCharset.decode(buff).toString
  }

  /**
   * Creates or updates user defined String attribute.
   * @param path denotes file or dir for which attribute will be added or updated
   * @param name new or existing attribute's identifier
   * @param value to be added/updated
   * @return true if operation succeeds, false otherwise
   */
  def writeUserStrAttr(path: Path, name: String, value: String): Boolean = {
    tryo {
      val view = getUserAttrView(path)
      view.write(name, Charset.defaultCharset().encode(value))
    } === value.getBytes.size // does the number of bytes written match expected value?
  }

  /**
   * Attempt to read user defined Int attribute.
   * @param path denotes file or dir to be queried
   * @param name attribute's identifier
   * @return attribute's value as Box[Int]
   */
  def readUserIntAttr(path: Path, name: String): Box[Int] = tryo {
    val view = getUserAttrView(path)
    val buff = ByteBuffer.allocate(4)
    view.read(name, buff)
    buff.flip.asInstanceOf[ByteBuffer].asIntBuffer
    buff.getInt
  }

  /**
   * Creates or updates user defined Int attribute.
   * @param path denotes file or dir for which attribute will be added or updated
   * @param name new or existing attribute's identifier
   * @param value to be added/updated
   * @return true if operation succeeds, false otherwise
   */
  def writeUserIntAttr(path: Path, name: String, value: Int): Boolean = tryo {
    val view = getUserAttrView(path)
    val buff = ByteBuffer.allocate(4)
    buff.putInt(value)
    buff.position(0)
    view.write(name, buff)
  } === 4 // does the number of bytes written match expected value?

}