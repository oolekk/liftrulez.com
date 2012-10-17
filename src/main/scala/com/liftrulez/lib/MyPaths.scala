package com.liftrulez.lib

import java.nio.file.Paths

import net.liftweb.util.Props

/**
 * Collect some often used directory paths inside this object.
 */
object MyPaths {

  val tmpdir =
    Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath

  val userdir =
    Paths.get(System.getProperty("user.dir")).toAbsolutePath

  val workdir =
    Paths.get(Props.get("jetty.emb.workdir", System.getProperty("user.dir"))).toAbsolutePath

  val userhome =
    Paths.get(System.getProperty("user.home")).toAbsolutePath

}