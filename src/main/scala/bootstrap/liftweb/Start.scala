package bootstrap.liftweb

import java.io.File
import net.liftweb.common._
import net.liftweb.util.Props
import net.liftweb.util.Helpers._
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext

/**
 * This runnable object is pointed to by sbt-assembly plugin, as one which will
 * be executed when jar containing this webapp is run. Its task is to configure
 * and start embedded jetty instance, which will then serve this webapp. Resources
 * external to the webapp can also be served by defining additional resource handlers.
 */
object Start extends Logger {

  /**
   * Instruct logback to use config files from props dir.Configure embedded jetty instance.
   * Decide which port number to use.  Set up resource handlers. Start jetty server.
   * @param args command line numeric parameter can be specified to make jetty use
   * specific port, this will override defaults or settings in props files. It is
   * however recommended to use props files to set jetty port.
   */
  def main(args: Array[String]): Unit = {

    /* Basic way to start the jar is:
     * java -jar myjarname.jar
     * You can adjust run.mode when starting the jar like so:
     * java -Drun.mode=production -jar myjarname.jar
     * You can also give numeric parameter to decide what port to use:
     * java -Drun.mode=production -jar myjarname.jar 8090
     */

    /* Calculate run.mode dependent path to logback configuration file, and set
     * system property accordingly. Use same naming scheme as for props files.
     * TO WORK IT NEEDS TO BE DONE EARLY, before system property is first read by
     * logback. If it won't find it right after it tarts it will fall-back to
     * defaults and not bother reading our configuration files. Then all is lost. */
    System.setProperty("logback.configurationFile", {
      val propsDir = "props"
      val fileNameTail = "default.logback.xml"
      (Box !! System.getProperty("run.mode")).
        dmap(propsDir + "/" + fileNameTail)(propsDir + "/" + _ + "." + fileNameTail)
    })

    /* Choose different port for each of your webapps deployed on a single machine.
     * You may then use it in nginx proxy-pass directive, to target virtual hosts.
     * If command line numeric parameter is given, it will be used for the port number.
     * Otherwise we will attempt to read jetty.emb.port property from props file or
     * use 9090 as ultimate fall-back. */
    val port = {
      tryo { args(0).toInt }.filter(portNumber ⇒ portNumber > 0 && portNumber < 65536)
    }.getOrElse(Props.getInt("jetty.emb.port", 9090))

    val connector = new SelectChannelConnector()
    connector.setPort(port)
    val server = new Server()
    server.addConnector(connector)

    val webctx = new WebAppContext
    /* Use embedded webapp dir as source of content to be served. */
    val webappDirFromJar = webctx.getClass.getClassLoader.getResource("webapp").toExternalForm
    webctx.setWar(webappDirFromJar)
    /* We might use use external, already existing webapp dir instead of
     * referencing the webapp dir from the jar but it's not very useful. 
     * I put it here for reference, as it may make sense under some circumstances.
     * webctx.setResourceBase("/path/to/existing/webapp-dir") */
    webctx.setContextPath("/")
    /* Below we extract embedded webapp to specific temporary location and serve
     * from there. Often /tmp is used, but it is not always advisable, because it
     * gets cleaned-up from time to time, so you need to use other location such as
     * /var/www/liftrulez.com for anything that should last. Our webapp will be then
     * deployed into /var/www/liftrulez.com/webapp and sibling directories can be
     * used to store other useful resources. */
    Props.get("jetty.emb.tmpdir").foreach(dir ⇒ {
      webctx.setTempDirectory(new File(dir))
      info("USING TEMP DIRECTORY: " + webctx.getTempDirectory)
    })

    //    server.setHandler(webctx)
    //    server.start
    //    server.join
    //
    //    /* And here is a little gem, which took me much time and experimentation.
    //     * To serve arbitrary resource directory (may be located outside webapp dir) 
    //     * use code below instead of previous block. This can be indispensable if 
    //     * your app uses some resources, which should not be put inside the executable
    //     * jar, because they are large, or change frequently, but should still be
    //     * accessible when jar is started. For example, this is very useful to access
    //     * a directory containing photo gallery pictures, kept separately, where stuff
    //     * can easily change when new pics are uploaded. */

    /* To demonstrate this we will expose two directories, which can be in
     * pretty much any arbitrary location on the file system. this is good if
     * we don't want these resorces beign cleaned up by jetty when we redeploy
     * our webapp.
     */
    val resHandlerOne = makeResourceHandler("/served", "/var/www/liftrulez.com/external-resources/served", true, false)
    val resHandlerTwo = makeResourceHandler("/listed", "/var/www/luftrulez.com/external-resources/listed", true, false)
    val handlerList = new HandlerList()
    /* IMPORTANT: order in the array matters, webctx should come last */
    handlerList.setHandlers(Array(resHandlerOne, resHandlerTwo, webctx))

    server.setHandler(handlerList)
    server.start
    server.join

  }

  /**
   * Create resource handler to serve resources from arbitrary location.
   * @param ctxPath context  path to be used by handler
   * @param resBase denotes location of the resources
   * @param listDirs_? decides weather we should serve directory listing pages
   * @param symlinks_? decides weather we should allow symlinks traversal
   * @return preconfigured resource handler instance
   */
  def makeResourceHandler(
    ctxPath: String, resBase: String,
    listDirs_? : Boolean = false, symlinks_? : Boolean = false) = {
    info("MAKE RESOURCE HANDLER ctxPath:%s resBase:%s listDirs:%s allowAliases:%s".
      format(ctxPath, resBase, listDirs_?, symlinks_?))

    val resHandler = new ResourceHandler()
    resHandler.setResourceBase(resBase)
    /* Normally only files can be accessed directly, but we may optionally allow
     * accessing dirs through the browser and serve pages with a list of dir contents. */
    resHandler.setDirectoriesListed(listDirs_?)

    val resCtxHandler = new ContextHandler()
    resCtxHandler.setContextPath(ctxPath)
    /* Enabling this will make aliases and symbolic links work,
     * which is turned off by default to avoid security risks. */
    resCtxHandler.setAliases(symlinks_?)
    resCtxHandler.setHandler(resHandler)

    resCtxHandler
  }

}