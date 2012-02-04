package org.dynasoar.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.dynasoar.comm.EventHandler;
import org.dynasoar.comm.NodeCommunicator;
import org.dynasoar.config.Configuration;
import org.dynasoar.sync.DirectoryWatcher;
import org.dynasoar.sync.ServiceConfigChangeEvent;
import org.dynasoar.sync.SyncEvent;
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
		DirectoryWatcher watcher = new DirectoryWatcher(
				new ServiceConfigChangeEvent());
		watcher.watch(serviceConfigDirPath);

		// TODO: Thread loop
		Thread thisThread = Thread.currentThread();
		while (thisThread == th) {
			logger.info("Check if this loops too often.");
		}

		// Handle clean exit
		watcher.exit();
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

		ServiceChangeEvent scEvent = (ServiceChangeEvent) event;
		DynasoarService service = scEvent.getService();

		try {
			// Update local service registry and take necessary actions
			switch (scEvent.getType()) {
			case ADDED:
				this.addOrUpdateService(service);
				break;
			case CHANGED:
				this.addOrUpdateService(service);
				break;
			case REMOVED:
				this.removeService(service.getShortName());
				break;
			case DEPLOYED:
			case REDEPLOYED:
				this.deploy(service.getShortName());
				break;
			case UNDEPLOYED:
				this.undeploy(service.getShortName());
				NodeCommunicator.newEvent(new ServiceChangeEvent(service,
						ServiceEventType.UNDEPLOYED));
				break;
			}
		} catch (ServiceDeployException e) {
			logger.error("An error occurred while deploying service.", e);
		} catch (IOException e) {
			logger.error("An error occurred while copying WAR package.", e);
		}

		// Emit a SyncEvent to synchronize the changes to other nodes
		NodeCommunicator.newEvent(new SyncEvent(scEvent.getService(), scEvent
				.getType()));
	}

	private void addOrUpdateService(DynasoarService service) {
		DynasoarService existing = serviceMap.get(service.getShortName());

		if (existing == null) {
			serviceMap.put(service.getShortName(), service);
		} else {
			serviceMap
					.put(service.getShortName(), existing.updateNode(service));
		}

		// Deploy it if required
		if (service.isDeployed()) {
			NodeCommunicator.newEvent(new ServiceChangeEvent(service,
					ServiceEventType.DEPLOYED));
		} else {
			NodeCommunicator.newEvent(new ServiceChangeEvent(service,
					ServiceEventType.UNDEPLOYED));
		}
	}

	private void removeService(String serviceName) {
		serviceMap.remove(serviceName);
	}

	private void deploy(String serviceName) throws ServiceDeployException,
			IOException {
		DynasoarService service = serviceMap.get(serviceName);

		if (service == null)
			throw new ServiceDeployException("Service not found");

		Path warFilePath = Paths.get(
				Configuration.getConfig("servicePackageDir"), serviceName
						+ ".war");
		Path destinationPath = Paths.get(Configuration.getConfig("deployDir"));

		if (!warFilePath.toFile().exists()) {
			throw new ServiceDeployException("WAR Package does not exist.");
		}

		Files.copy(warFilePath, destinationPath,
				StandardCopyOption.ATOMIC_MOVE,
				StandardCopyOption.REPLACE_EXISTING);
	}

	private void undeploy(String serviceName) throws IOException {
		Path deployPath = Paths.get(Configuration.getConfig("deployDir"),
				serviceName + ".war");

		Files.deleteIfExists(deployPath);
	}

}
