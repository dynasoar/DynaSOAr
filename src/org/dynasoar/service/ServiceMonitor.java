package org.dynasoar.service;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.dynasoar.comm.EventHandler;
import org.dynasoar.comm.NodeCommunicator;
import org.dynasoar.config.Configuration;
import org.dynasoar.sync.ChangeEvent;
import org.dynasoar.sync.DirectoryWatcher;
import org.dynasoar.util.Event;

/**
 * ServiceMonitor is responsible for monitoring changes in Service config files.
 * It is supposed to act on and notify NodeCommunicator of any change in
 * Service.
 * 
 * @author Rakshit Menpara
 */
public class ServiceMonitor extends EventHandler implements Runnable {

	private static ServiceMonitor current = null;
	private static Logger logger = Logger.getLogger(ServiceMonitor.class);
	private static Thread th = null;
	private static HashMap<String, DynasoarService> serviceMap = new HashMap<String, DynasoarService>();

	public ServiceMonitor() {
		super(ServiceChangeEvent.class);
	}

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

		// Read "ServiceConfigDir" from configuration and starts listening
		// to the directory
		String serviceConfigDirPath = Configuration
				.getConfig("serviceConfigDir");
		DirectoryWatcher dir = new DirectoryWatcher(
				new ServiceConfigChangeEvent());
		dir.watch(serviceConfigDirPath);

		// TODO: Thread loop
		Thread thisThread = Thread.currentThread();
		while (thisThread == th) {
			// In case of any changes in directory, Read service config file,
			// load/re-deploy the service on local server

			// Notify NodeCommunicator of all the changes occurred

		}

		// Handle clean exit
		dir.exit();
		logger.info("ServiceMonitor shutdown complete");
	}

	public static void shutdown() {
		logger.info("Shutting down ServiceMonitor.");
		th = null;
	}

	public static void newEvent(Event event) {
		if (current != null) {
			current.emitEvent(event);
		}
	}

	/**
	 * {@link EventHandler#handle(Event)} implementation.
	 */
	@Override
	public void handle(Event event) {
		logger.info("Processing an event: " + event.toString());

		// Delegate handling of the event if possible
		if (event instanceof ServiceChangeEvent) {

		}
	}

	/**
	 * Implements ChangeEvent interface, which will handle directory change
	 * events of Service Config Directory
	 * 
	 * @author Rakshit Menpara
	 */
	public static class ServiceConfigChangeEvent implements ChangeEvent {

		@Override
		public void fileCreated(String path) {
			DynasoarService service = this.readServiceConfig(path);

			if (service != null) {
				serviceMap.put(service.getShortName(), service);
				NodeCommunicator.newEvent(new ServiceChangeEvent(service,
						ServiceEventType.ADDED));
				logger.info("Service added - " + service.getName());
			} else {
				logger.info("Service Config File Null");
			}
		}

		@Override
		public void fileModified(String path) {
			DynasoarService service = this.readServiceConfig(path);

			if (service != null) {
				serviceMap.put(service.getShortName(), service);
				logger.info("Service changed - " + service.getName());
				NodeCommunicator.newEvent(new ServiceChangeEvent(service,
						ServiceEventType.CHANGED));
			} else {
				logger.info("Service Config File Null");
			}
		}

		@Override
		public void fileRemoved(String path) {
			// TODO: Correct
			DynasoarService service = this.readServiceConfig(path);

			if (service != null) {
				serviceMap.remove(service.getShortName());
				logger.info("Service removed - " + service.getName());
				NodeCommunicator.newEvent(new ServiceChangeEvent(service,
						ServiceEventType.REMOVED));
			} else {
				logger.info("Service Config File Null");
			}
		}

		private DynasoarService readServiceConfig(String path) {
			// Read and parse the config file using JSON parser (jackson)
			DynasoarService service = null;
			File configFile = new File(path);
			ObjectMapper mapper = new ObjectMapper();
			try {
				service = mapper.readValue(configFile, DynasoarService.class);
				service.setShortName(configFile.getName());
			} catch (Exception e) {
				logger.error("ServiceConfig parsing failed.", e);
			}

			return service;
		}
	}
}
