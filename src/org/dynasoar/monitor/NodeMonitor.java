package org.dynasoar.monitor;

import org.apache.log4j.Logger;
import org.dynasoar.comm.NodeCommunicator;
import org.dynasoar.service.ServiceMonitor;
import org.dynasoar.webserver.WebServer;

/**
 * NodeMonitor is responsible for monitoring current node and make sure all the
 * supporting threads are running.
 * 
 * @author Rakshit Menpara
 */
public class NodeMonitor {
	private static Logger logger = Logger.getLogger(NodeMonitor.class);

	private static boolean shutdown = false;
	private static int interval = 1000;

	public static void start() {
		while (!shutdown) {
			if (!NodeCommunicator.isRunning()) {
				// Invoke TCP communication thread
				NodeCommunicator.start();
			}

			if (!ServiceMonitor.isRunning()) {
				// Invoke the service repository monitor
				ServiceMonitor.start();
			}

			if (!WebServer.isRunning()) {
				// Start embedded Jetty Webserver
				WebServer.start();
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				logger.error("Thread busy.", e);
			}
		}

		// Cleanly exit all threads when shutdown signal is sent
		NodeCommunicator.shutdown();
		ServiceMonitor.shutdown();
		WebServer.shutdown();

		logger.info("NodeMonitor shutdown complete.");
	}

	public static void shutdown() {
		logger.info("Shutting Down NodeMonitor");
		shutdown = true;
	}
}
