package com.liftrulez.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel
import net.liftweb.http.RequestVar
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJE.Jq
import net.liftweb.json._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonParser._
import java.nio.file.Path
import java.nio.file.Paths
import scala.xml.NodeSeq
import scala.xml.Elem
import com.liftrulez.lib._

/** UNDER CONSTRUCTION **/

/**
 * This class represents single directory pane.
 * @param fsBase defines default location to be opened initially if no
 * specific path is requested. It is also the highest reachable directory
 * for this DirBrowser.
 * @param dsplyBase determines what base will be shown to the client. Usually we
 * won't make user aware in what specific directory on server he is in, then
 * we might use "", "/" string or "/username/" for that. Depends on the situation.
 * @param perms not used yet, will be used to determine what operations are permitted.
 * We may use it to implement read-only version of DirBrowser
 */
class DirBrowser(
  val fsBase: Path,
  val perms: String = "",
  val dsplyBase: String)

/**
 * To be part of a group, DirBrowser must hold reference to the
 * group and have its identifier, enabling it to query group level
 * functions with it's name as a parameter, to receive personalized
 * response.
 * @param name identifier of this instance within DirBrowserGroup
 * @param group reference to the group to which this instance belongs
 */
class GroupDirBrowser(
  val name: String,
  val group: DirBrowserGroup,
  fsBase: Path, dsplyBase: String = "", perms: String = "")
  extends DirBrowser(fsBase, dsplyBase, perms)

/**
 * We might someday need more than two panes, so instead of only
 * defining TwinDirBrowser object, we will use this class as a
 * general holder for GroupedDirBrowser, implementing logic that
 * needs to be done on the group level. TwinDirBrowser will extend
 * this class.
 */
class DirBrowserGroup extends DispatchSnippet {

  /**
   * Add new browser to this group.
   * @param browser new item to be added to this group
   */
  def addBrowser(browser: GroupDirBrowser): GroupDirBrowser = {
    browsers = browsers + (browser.name -> browser)
    browser
  }

  /**
   * May be used in the future if I find the need for some temporary
   * pop-up browser for example which we won't bother keeping.
   */
  def removeBrowser(browser: GroupDirBrowser): GroupDirBrowser = {
    browsers = browsers - browser.name
    browser
  }

  protected var browsers = Map[String, GroupDirBrowser]()

  /**
   * Here we collect browser-name -> current-sub-path pairs for all
   * the browsers. We can thus take note of which directory is currently
   * the displayed one in each of them.
   */
  object CurrentNameLocMap extends RequestVar[Box[Map[String, String]]](Empty)

  /**
   * Here we update CurrentLocations to be in place for the next request to come
   */
  def setReqVars() = {
    val currentNameLocMap = browsers.keys.map(
      /* search for current location of each browser,
       * which we should find in request params */
      name ⇒ S.param("name").map((name, _))).map(_.toMap).headOption
    /* what's been found gets stored in CurrentLocations */
    currentNameLocMap.map(x ⇒ CurrentNameLocMap(Full(x)))
  }

  /* THE DISPATCHER PART */

  def dispatch = {
    case _ ⇒ render _
  }

  def render(ns: NodeSeq): NodeSeq = {
    setReqVars()
    <h1> NOT IMPLEMENTED YET </h1>
    //    (renderPanesCssSel & tailScriptCssSel).apply(ns)
  }

  //   // SET UP ON LOAD EVENT BINDING
  //  
  //  /**
  //   * Add onLoad script at the end of the page to bind click
  //   * events to the buttons.
  //   */
  //  def tailScriptCS: CssSel = {
  //    ".tailScript" #>
  //      <l:tail>{
  //        Script({
  //          OnLoad(
  //            clickCall(".dirCreateBttn", "newFodMsg", procNewDirJson _) &
  //              clickCall(".fileCreateBttn", "newFodMsg", procNewFileJson _))
  //        })
  //      }</l:tail>
  //  }

}
/**
 * This is a version of DirBrowserGroup using two panes, which will
 * probably be the most frequently used implementation for this webapp
 */
object TwinDirBrowser extends DirBrowserGroup with DispatchSnippet {

  val left = new GroupDirBrowser("left", this, MyPaths.workdir)
  val right = new GroupDirBrowser("right", this, MyPaths.tmpdir)

  addBrowser(left)
  addBrowser(right)

}