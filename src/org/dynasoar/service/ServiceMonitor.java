package org.dynasoar.service;

import org.apache.log4j.Logger;
import org.dynasoar.dirWatcher.DirectoryWatcher;
import org.dynasoar.config.Configuration;

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
	DirectoryWatcher dir = new DirectoryWatcher();
	public static void start() {
		// TODO: Start this in a separate thread
		current = new ServiceMonitor();
		th = new Thread(current, "ServiceMonitor");
		th.start();
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
		String path = Configuration.getConfig("serviceConfigDir");
		
		while(true){
			dir.watch(path);
		}
		// In case of any changes in directory, Read service config file,
		// load/re-deploy the service on local server

		// Notify NodeCommunicator of all the changes occurred

	}
}
