package com.liftrulez.lib

import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult._

/**
 * Counts directories, files and their combined size.
 * To be used as argument to walkDirectoryTree
 */
class BasicStatsVisitor extends SimpleFileVisitor[Path] {
  var dirCount = 0
  var fileCount = 0
  var filesSize = 0L
  var failed = 0

  def report = (dirCount, fileCount, filesSize, failed)

  def reportStr = "dirs count:%s\nfiles count:%s\ntotal bytes:%s".format(
    dirCount, fileCount, filesSize) + {
      if (failed == 0) "\n"
      else "\ncould not visit " + failed + " entries\n"
    }

  override def visitFile(file: Path, attrs: BasicFileAttributes) = {
    fileCount = fileCount + 1
    filesSize = filesSize + attrs.size
    CONTINUE
  }

  override def preVisitDirectory(file: Path, attrs: BasicFileAttributes) = {
    dirCount = dirCount + 1
    CONTINUE
  }

  override def visitFileFailed(file: Path, e: java.io.IOException) = {
    failed = failed + 1
    CONTINUE
  }

}

/**
 * Here we will provide deep copy logic, to copy recursively all
 * files and directories.
 */
class DeepCopyVisitor(val termOnFail_? : Boolean = true) extends SimpleFileVisitor[Path] {

}

/**
 * This one is my favorite (although not done yet)
 * Here we will provide clone copy logic, to copy recursively only
 * directories. Files will be hard-linked, creating independent
 * pointers to the given inode. Can be very useful when we need to
 * have same images in many places. This will only take about as much
 * space as a single file. Also there is no problem with synchronizing
 * these cloned files contents. Changes will propagate to all clones
 * without any effort. (Actually they don't even need to propagate,
 * very cool :)
 */
class CloneCopyVisitor extends SimpleFileVisitor[Path]

/**
 * Placeholder for recursive delete logic.
 */
class DeleteAllVisitor extends SimpleFileVisitor[Path]
