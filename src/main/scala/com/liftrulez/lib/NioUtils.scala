package com.liftrulez.lib

import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import net.liftweb.common._
import net.liftweb.util.ControlHelpers._
import scala.collection.mutable.ListBuffer

/**
 * Provides box-wrapped java.nio file system operations.
 */
object NioUtils {

  /**
   * Convert String to a Box[Path]
   */
  implicit def stringToPathBox(path: String): Box[Path] =
    tryo(Paths.get(path))

  /**
   * Convert Path to a Box[Path]
   */
  implicit def pathToPathBox(path: Path): Box[Path] =
    Box !! path

  /**
   * Create glob path matcher.
   * @param pattern string used to construct the pattern
   * @return glob path matcher
   */
  def globMatcher(pattern: String): PathMatcher =
    FileSystems.getDefault().getPathMatcher("glob:" + pattern)

  /**
   * Create regex path matcher.
   * @param pattern string used to construct the pattern
   * @return regex path matcher
   */
  def regexMatcher(pattern: String): PathMatcher =
    FileSystems.getDefault().getPathMatcher("regex:" + pattern)

  /**
   * Check that path points to existing filesystem item. Note that file system
   * query is immediately outdated, so there is no guarantee that item exist afterwards.
   * @param path denotes path to be queried
   * @return Full(path) if file system  query confirmed it points to an existing file
   * system item Failure otherwise
   */
  def existsBox(path: Box[Path]): Box[Path] =
    path.filterMsg("can't confirm that path points to an existing item")(Files.exists(_))

  /**
   * Check that path points to existing directory. Note that file system
   * query is immediately outdated, so there is no guarantee that item exist afterwards.
   * @param path denotes path to be queried
   * @return Full(path) if file system  query confirmed it points to an existing directory
   * Failure otherwise
   */
  def isDirBox(path: Box[Path]): Box[Path] =
    path.filterMsg("can't confirm that path points to an existing directory")(Files.isDirectory(_))

  /**
   * Check that path points to existing regular file. Note that file system
   * query is immediately outdated, so there is no guarantee that item exist afterwards.
   * @param path denotes path to be queried
   * @return Full(path) if file system  query confirmed it points to an existing regular file
   * Failure otherwise
   */
  def isRegFileBox(path: Box[Path]): Box[Path] =
    path.filterMsg("can't confirm that path points to an existing regular file")(Files.isRegularFile(_))

  /**
   * Attempt to create directory.
   * @param path denotes directory to be created
   * @return Full(path) pointing to this new directory on success, Failure otherwise
   */
  def createDirBox(path: Box[Path]): Box[Path] =
    path.flatMap(dir ⇒ tryo { Files.createDirectory(dir) })

  /**
   * Attempt to create directory plus any necessary parent directories.
   * @param path denotes directory to be created
   * @return Full(path) pointing to this new directory on success, Failure otherwise
   */
  def createDirsBox(path: Box[Path]): Box[Path] =
    path.flatMap(dir ⇒ tryo { Files.createDirectories(dir) })

  /**
   * Attempt to create new empty file.
   * @param path denotes file to be created
   * @return Full(path) pointing to this new file on success, Failure otherwise.
   */
  def createFileBox(path: Box[Path]) =
    path.flatMap(file ⇒ tryo { Files.createFile(file) })

  /**
   * Attempt to create new symlink to a given target.
   * @param link symlink to be created
   * @param target symlink's target
   * @return Full(link) representing the symlink on success, Failure otherwise.
   */
  def createSymLinkBox(target: Box[Path])(link: Box[Path]): Box[Path] =
    target.flatMap(tgt ⇒ link.flatMap(lnk ⇒ tryo { Files.createSymbolicLink(lnk, tgt) }))

  /**
   * Attempt to create new hardlink to a given target.
   * @param link hardlink to be created
   * @param target hardlink's target
   * @return Full(link) representing the hardlink on success, Failure otherwise.
   */
  def createHardLinkBox(target: Box[Path])(link: Box[Path]): Box[Path] =
    target.flatMap(tgt ⇒ link.flatMap(lnk ⇒ tryo { Files.createLink(lnk, tgt) }))

  /**
   * Attempt to delete file system item represented by the given path.
   * @param path file system item to be removed
   * @return Full(path) representing removed item on success, Failure otherwise
   */
  def deletePathBox(path: Box[Path]): Box[Path] = {
    path.flatMap(file ⇒ tryo {
      Files.delete(file)
      /* return path to now not existing file */
      file
    })
  }

  /**
   * Attempt to delete file system item represented by the given path.
   * @param path file system item to be removed
   * @return Full(path) representing removed item on success, Failure otherwise
   */
  def deleteIfExistsPathBox(path: Box[Path]): Box[Path] = {
    path.flatMap(file ⇒ tryo {
      Files.deleteIfExists(file)
      /* return path to now not existing file */
      file
    })
  }

  /**
   * Attempt to delete path target, after checking that it represents a regular file.
   * If path is a symlink to a file, that symlink will be removed while its
   * target will not be affected.
   * @param regular file to be removed
   * @return Full(path) representing removed item on success, Failure otherwise
   */
  def deleteFileBox(path: Box[Path]): Box[Path] = {
    isRegFileBox(path).flatMap(file ⇒ tryo {
      Files.delete(file)
      /* return path representing the removed file */
      file
    })
  }

  /**
   * Attempt to delete path target, after checking that it represents a directory.
   * If path is a symlink to a directory, that symlink will be removed while its
   * target will not be affected.
   * @param regular file to be removed
   * @return Full(path) representing removed item on success, Failure otherwise
   */
  def deleteDirBox(path: Box[Path]): Box[Path] = {
    isDirBox(path).flatMap(file ⇒ tryo {
      Files.delete(file)
      /* return path representing the removed file */
      file
    })
  }

  /**
   * Attempt to copy from source to target.
   * @param source denotes source path
   * @param target denotes target path
   * @param options additional options for the copy operation
   * @return Full(target) if copy succeeds, Failure otherwise
   */
  def copyPathBox(source: Box[Path], target: Box[Path], options: java.nio.file.CopyOption*): Box[Path] = {
    source.flatMap(src ⇒ target.flatMap(tgt ⇒ tryo { Files.copy(src, tgt, options: _*) }))
  }

  /**
   * Attempt to move from source to target.
   * @param source denotes source path
   * @param target denotes target path
   * @param options additional options for the move operation
   * @return Full(target) if move succeeds, Failure otherwise
   */
  def movePathBox(source: Box[Path], target: Box[Path], options: java.nio.file.CopyOption*): Box[Path] = {
    source.flatMap(src ⇒ target.flatMap(tgt ⇒ tryo { Files.move(src, tgt, options: _*) }))
  }

  /**
   * Acquire boxed Iterator[Path] over the filtered contents of a given directory.
   * @param dir directory to query
   * @param predicate used to filter entries, by default all items are accepted
   * @return boxed iterator over filtered directory contents
   */
  def pathIteratorBox(dir: Box[Path], predicate: Path ⇒ Boolean = (path: Path) ⇒ true): Box[java.util.Iterator[Path]] = {
    val filter = new java.nio.file.DirectoryStream.Filter[Path] {
      def accept(entry: Path) = predicate(entry)
    }
    dir.flatMap(d ⇒ tryo { Files.newDirectoryStream(d, filter).iterator })
  }

  /**
   * Acquire boxed List[Path] representing filtered directory contents.
   * @param dir directory to query
   * @param predicate used to filter entries, by default all items are accepted
   * @return boxed list of filtered directory contents
   */
  def dirItemsAsList(dir: Box[Path], predicate: Path ⇒ Boolean = (path: Path) ⇒ true): Box[List[Path]] =
    dir.flatMap(pathIteratorBox(_, predicate).map(it ⇒ {
      val buffer = ListBuffer[Path]()
      while (it.hasNext) buffer += it.next
      buffer.toList
    }))

  /**
   * Acquire a pair of List[Path] representing directory contents, where
   * the first list contains only items recognized as directories
   * and the second list contains remaining items.
   * @param dir directory to query
   * @return boxed pair of lists, first contains directory paths, second paths of the remaining items
   */
  def listDirItemsSplit(dir: Box[Path]): Box[(List[Path], List[Path])] =
    dirItemsAsList(dir).map(_.partition(Files.isDirectory(_)))

  /**
   * Acquire boxed List[Path] representing directory contents which are recognized as directories.
   * @param dir directory to query
   * @return boxed list of directories paths
   */
  def listDirDirs(dir: Box[Path]): Box[List[Path]] =
    dirItemsAsList(dir, (path: Path) ⇒ Files.isDirectory(path))

  /**
   * Acquire boxed List[Path] representing directory contents which are recognized as regular files.
   * @param dir directory to query
   * @return boxed list of regular files paths
   */
  def listDirRegFiles(dir: Box[Path]): Box[List[Path]] =
    dirItemsAsList(dir, (path: Path) ⇒ Files.isRegularFile(path))

  def printTreeStats(startAt: Box[Path]) =
    startAt.map(path ⇒ {
      val fv = new BasicStatsVisitor
      Files.walkFileTree(path, fv)
      fv.report
    })

}
