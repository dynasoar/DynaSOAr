package org.dynasoar.webserver;

import java.io.File;

import org.apache.log4j.Logger;
import org.dynasoar.communicator.NodeCommunicator;
import org.dynasoar.config.Configuration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

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

			while (jettyServer != null) {
				// Do something

				logger.info("Alive");
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			logger.error("Jetty server error", e);
		}
	}

	private Server startServer() throws Exception {
		Server jettyServer = new Server(Integer.parseInt(Configuration
				.getConfig("webServerPort")));

		WebAppContext webapp = new WebAppContext();
		File warPath = new File(Configuration.getConfig("deployDir"));

		webapp.setClassLoader(Thread.currentThread().getContextClassLoader());
		webapp.setContextPath("/");
		webapp.setWar(warPath.getAbsolutePath());

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { webapp, new DefaultHandler() });
		jettyServer.setHandler(handlers);

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
