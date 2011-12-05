package org.dynasoar.service;

import org.apache.log4j.Logger;

/**
 * ServiceMonitor is responsible for monitoring changes in Service config files.
 * It is supposed to act on and notify NodeCommunicator of any change in
 * Service.
 * 
 * @author Rakshit Menpara
 */
public class ServiceMonitor implements Runnable {
	private static ServiceMonitor current = null;
	private static Logger logger = Logger.getLogger(ServiceMonitor.class);
	private static Thread th = null;

	public static void start() {
		// TODO: Start this in a separate thread
	}

	public static boolean isRunning() {
		if (current == null) {
			return false;
		}

		return th.isAlive();
	}

	@Override
	public void run() {
		// TODO: Read "ServiceConfigDir" from configuration and start listening
		// to the directory

		// In case of any changes in directory, Read service config file,
		// load/re-deploy the service on local server

		// Notify NodeCommunicator of all the changes occurred
	}
}
