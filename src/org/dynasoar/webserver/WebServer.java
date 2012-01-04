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

    public static void start() {
        current = new WebServer();
        th = new Thread(current, "WebServer");
        th.start();
    }

	@Override
	public void run() {
		try {
			Server jettyServer = this.startServer();
			
			// Sleep while waiting for server to start
			while(!jettyServer.isStarted()) {
				Thread.sleep(500);
			}
			
			// TODO: Notify the communicator once started
			
		} catch (Exception e) {
			logger.error("Jetty server error", e);
		}
	}

	private Server startServer() throws Exception {
		Server jettyServer = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		File warPath = new File(Configuration.getConfig("deployDir"));

		webapp.setClassLoader(Thread.currentThread().getContextClassLoader());
		webapp.setContextPath("/");
		webapp.setWar(warPath.getAbsolutePath());

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { webapp, new DefaultHandler() });
	    jettyServer.setHandler(handlers);

		jettyServer.start();
		jettyServer.join();
		
		return jettyServer;
	}

    public static boolean isRunning() {
        if (current == null) {
            return false;
        }

        return th.isAlive();
    }
}
