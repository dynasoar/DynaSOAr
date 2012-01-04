package org.dynasoar.monitor;

import org.apache.log4j.Logger;
import org.dynasoar.communicator.NodeCommunicator;
import org.dynasoar.service.ServiceMonitor;
import org.dynasoar.webserver.WebServer;

/**
 * NodeMonitor is responsible for monitoring current node. Along with monitoring
 * Node's load and services, it will also make sure all the supporting threads
 * are running.
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
			
			if(!WebServer.isRunning()) {
				// Start embedded Jetty Webserver
				WebServer.start();
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				logger.error("Thread busy.", e);
			}
		}
	}

	public static void shutdown() {
		logger.info("Shutting Down NodeMonitor");
		shutdown = true;
	}
}
