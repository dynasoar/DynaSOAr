package org.dynasoar.monitor;

import org.apache.log4j.Logger;
import org.dynasoar.communicator.NodeCommunicator;
import org.dynasoar.config.Configuration;
import org.dynasoar.service.ServiceMonitor;

/**
 * NodeMonitor is responsible for monitoring current node. Along with monitoring
 * Node's load and services, it will also make sure all the supporting threads
 * are running.
 * 
 * @author Rakshit Menpara
 */
public class NodeMonitor implements Runnable {
	private static NodeMonitor current = null;
	private static Logger logger = Logger.getLogger(NodeMonitor.class);
	private static Thread th = null;

	private boolean shutdown = false;
	private int interval = 1000;

	public static void start() {
		current = new NodeMonitor();
		Thread th = new Thread(current, "NodeMonitor");
		th.start();
	}

	public static NodeMonitor get() {
		if (current == null) {
			start();
		}
		return current;
	}

	public static boolean isRunning() {
		if (current == null) {
			return false;
		}

		return th.isAlive();
	}

	@Override
	public void run() {
		while (!shutdown) {
			if(!NodeCommunicator.isRunning()) {
				
			}
			// Invoke TCP communication thread
			NodeCommunicator.start();

			// Invoke the service repository monitor
			ServiceMonitor.start();

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				logger.error("Thread busy.", e);
			}
		}
	}

	public void shutdown() {
		logger.info("Shutting Down NodeMonitor");
		shutdown = true;
	}
}
