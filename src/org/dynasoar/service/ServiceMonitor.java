package org.dynasoar.service;

/**
 * ServiceMonitor is responsible for monitoring changes in Service config files.
 * It is supposed to act on and notify NodeCommunicator of any change in
 * Service.
 * 
 * @author Rakshit Menpara
 */
public class ServiceMonitor implements Runnable {
	public static void start() {
		// TODO: Start this in a separate thread
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
