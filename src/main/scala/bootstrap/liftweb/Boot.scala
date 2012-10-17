package bootstrap.liftweb

import com.liftrulez.model.SchemaHelper
import org.slf4j.LoggerFactory
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props
import net.liftweb.util.NamedPF
import net.liftweb.http.LiftRules
import net.liftweb.http.Html5Properties
import net.liftweb.http.NoticeType
import net.liftweb.http.NotFoundAsTemplate
import net.liftweb.http.OnDiskFileParamHolder
import net.liftweb.http.Req
import net.liftweb.http.ParsePath
import net.liftweb.http.PostRequest
import net.liftweb.http.OkResponse
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap

/**
 * WebApp initialization logic.
 */
class Boot extends Logger {

  /**
   *  Set up LiftRules, snippet dispatching, sitemap, and trigger database initialization.
   */
  def boot {

    info("Run mode is: %s".format(Props.mode))

    /* Default package to search for snippets, views, comet */
    LiftRules.addToPackages("com.liftrulez")
    /* Force the request to be UTF-8 */
    LiftRules.early.append((req: HTTPRequest) ⇒ { req.setCharacterEncoding("UTF-8") })
    /* Use HTML5 templates */
    LiftRules.htmlProperties.default.set((r: Req) ⇒ new Html5Properties(r.userAgent))
    /* Store uploads as files on disk */
    LiftRules.handleMimeFile = OnDiskFileParamHolder.apply
    /* Set max total upload size */
    LiftRules.maxMimeSize = 1024 * 1024 * 32
    /* Set max per-file upload size */
    LiftRules.maxMimeFileSize = 1024 * 1024 * 32
    /* Use jQuery framework */
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQueryArtifacts
    /* Show the spinny image when an Ajax call starts */
    LiftRules.ajaxStart = Full(() ⇒ LiftRules.jsArtifacts.show("ajax-loader").cmd)
    /* Make the spinny image go away when it ends */
    LiftRules.ajaxEnd = Full(() ⇒ LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    /* Notice fade out (start to fade out after x, fade out duration y) */
    LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) ⇒ {
      notices match {
        case NoticeType.Notice  ⇒ Full((4 seconds, 2 seconds))
        case NoticeType.Warning ⇒ Full((6 seconds, 2 seconds))
        case NoticeType.Error   ⇒ Full((8 seconds, 2 seconds))
        case _                  ⇒ Empty
      }
    })
    /* set custom 404 handler */
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) ⇒
        NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    LiftRules.snippetDispatch.append {
      case "loginForm"        ⇒ com.liftrulez.snippet.LoginForm
      case "LeftRightBrowser" ⇒ com.liftrulez.snippet.TwinDirBrowser
    }

    initSitemap()

    SchemaHelper.initDB(
      Props.get("use.db", "H2"),
      Props.getBool("db.schemify", false))

    //    /* TEMPORAIRLY DISABLED LOGIN CHECK */
    //    LiftRules.loggedInTest = Full(() ⇒ LoginVar.get)
    //    val goToLogin = () ⇒ Full(RedirectResponse("/login"))
    //    LiftRules.dispatch.prepend(NamedPF("Login Page") {
    //      case Req("manager" :: _, "", _) if !LoginVar.get ⇒ goToLogin
    //    })

    /* Instruct Lift not to swallow things under /servlet/
     * so that in WEB-INF/web.xml we can attach java servlets there. */
    if (Props.getBool("show.servlets", false))
      LiftRules.liftRequest.append({
        case r if (r.path.partPath match {
          case "servlet" :: _ ⇒ true
          case _              ⇒ false
        }) ⇒ false
      })

  }

  private def initSitemap(uniqueLinks_? : Boolean = true) {
    SiteMap.enforceUniqueLinks = uniqueLinks_?
    val entries = List[Menu](
      Menu.i("Home") / "index",
      Menu.i("Login") / "login",
      Menu.i("Manager") / "manager")
    /* If you don't want access control for each page, comment this out */
    LiftRules.setSiteMap(SiteMap(entries: _*))
  }

}

