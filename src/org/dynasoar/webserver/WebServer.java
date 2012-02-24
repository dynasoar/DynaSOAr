package org.dynasoar.webserver;

import java.io.File;

import org.apache.log4j.Logger;
import org.dynasoar.comm.NodeCommunicator;
import org.dynasoar.config.Configuration;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class WebServer implements Runnable {

	private static Logger logger = Logger.getLogger(WebServer.class);
	private static WebServer current = null;
	private static Thread th = null;
	private static Server jettyServer = null;

	public static void start() {
		current = new WebServer();
		th = new Thread(current, "WebServer");
		th.start();
	}

	@Override
	public void run() {
		try {
			jettyServer = this.startServer();

			// Sleep while waiting for server to start
			while (!jettyServer.isStarted()) {
				Thread.sleep(500);
			}

			// TODO: Notify the communicator once started

			// Join server thread to prevent from exiting
			jettyServer.join();
		} catch (Exception e) {
			logger.error("Jetty server error", e);
		}
	}

	private Server startServer() throws Exception {
		Server jettyServer = new Server(Integer.parseInt(Configuration
				.getConfig("webServerPort")));

		/*WebAppContext webapp = new WebAppContext();
		File warPath = new File(Configuration.getConfig("deployDir"));

		System.out.println("Jetty WAR file path = " +warPath);
                webapp.setClassLoader(Thread.currentThread().getContextClassLoader());
		webapp.setContextPath("/");
		webapp.setWar(warPath.getAbsolutePath());
                System.out.println("Absolute path =" +warPath.getAbsolutePath().toString());
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { webapp, new DefaultHandler() });
		jettyServer.setHandler(handlers);*/
                
                File warPath = new File(Configuration.getConfig("deployDir"));
                
                HandlerCollection handlers = new HandlerCollection();
                ContextHandlerCollection contexts = new ContextHandlerCollection();
                handlers.setHandlers(new Handler[] { contexts, new DefaultHandler() });
                 
                 jettyServer.setHandler(handlers);
            
                 DeploymentManager deployer = new DeploymentManager();
                 deployer.setContexts(contexts);
            
                 jettyServer.addBean(deployer);

                 WebAppProvider webAppProvider = new WebAppProvider();
                  
                 webAppProvider.setExtractWars(true);
                 webAppProvider.setScanInterval(10);
                 webAppProvider.setMonitoredDirName(warPath.getAbsolutePath());
                 deployer.addAppProvider(webAppProvider);
                 
                 jettyServer.start();

		return jettyServer;
	}

	public static boolean isRunning() {
		if (current == null) {
			return false;
		}

		return th.isAlive();
	}

	public static void shutdown() {
		logger.info("Shutting down WebServer.");
		try {
			jettyServer.stop();
			while (!jettyServer.isStopped()) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			logger.error("Error while shutting down WebServer.", e);
		} finally {
			jettyServer = null;
			th = null;
			logger.info("WebServer shutdown complete.");
		}
	}
}
