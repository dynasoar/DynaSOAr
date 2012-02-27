package org.dynasoar.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.dynasoar.comm.EventHandler;
import org.dynasoar.comm.NodeCommunicator;
import org.dynasoar.config.Configuration;
import org.dynasoar.sync.DirectoryWatcher;
import org.dynasoar.sync.ServiceConfigChangeEvent;
import org.dynasoar.sync.SyncEvent;
import org.dynasoar.util.Event;
import org.dynasoar.webserver.WebServer;

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
		watcher.start(serviceConfigDirPath);

		// TODO: Thread loop
		try {
			Thread thisThread = Thread.currentThread();
			while (thisThread == th) {
				logger.info("Check if this loops too often.");
				this.processEvents();
				Thread.sleep(5000);
				// break;

			}
		} catch (InterruptedException e) {
			logger.error("Thread Interrupted" + e);
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
		// SyncEvent syncEvent = (SyncEvent) event;
		DynasoarService service = scEvent.getService();
		String WARchecksum = null;
		String Configchecksum = null;
		String WARPath = null;
		String ConfigPath = null;
		byte[] WARArray = null;

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

		// Calculating the MD5 hash of the WAR file and Config file of the
		// modified service

		try {
			WARPath = Paths.get(Configuration.getConfig("deployDir"),
					service.getShortName() + ".war").toString();
			ConfigPath = Paths.get(Configuration.getConfig("serviceConfigDir"),
					service.getShortName() + ".json").toString();

			Configchecksum = getMD5Checksum(ConfigPath);
			WARchecksum = getMD5Checksum(WARPath);
			WARArray = WARtoArray(WARPath);

			logger.info("WARfile MD5 hash checksum =" + WARchecksum);
			logger.info("Configfile MD5 hash checksum =" + Configchecksum);

		} catch (Exception ex) {
			logger.error("Cannot find the modified WAR file/Config file." + ex);
		}

		// Calculating the MD5 Hash of the config file and WAR file of the
		// modified service

		// syncEvent.setConfigMD5Hash(ConfigfilePath);
		// syncEvent.setWARMD5Hash(WARfilePath);
		// syncEvent.setWARfile(WARArray);

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

		if (service == null) {
			throw new ServiceDeployException("Service not found");
		}

		Path warFilePath = Paths.get(
				Configuration.getConfig("servicePackageDir"), serviceName
						+ ".war");
		Path destinationPath = Paths.get(Configuration.getConfig("deployDir"),
				"/" + serviceName + ".war");

		if (!warFilePath.toFile().exists()) {
			throw new ServiceDeployException("WAR Package does not exist.");
		}
		// Copies the service into deploy directory
		Files.copy(warFilePath, destinationPath,
				StandardCopyOption.REPLACE_EXISTING);
	}

	private void undeploy(String serviceName) throws IOException {
		Path deployPath = Paths.get(Configuration.getConfig("deployDir"),
				serviceName + ".war");

		Files.deleteIfExists(deployPath);
	}

	public static String getMD5Checksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();

		byte[] b = complete.digest();
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static byte[] WARtoArray(String filename) {
		byte[] bytes = null;
		try {

			File file = new File(filename);
			InputStream is = new FileInputStream(file);

			long length = 0;
			length = file.length();
			bytes = new byte[(int) length];
			int offset = 0;
			int numRead = 0;

			if (length > Integer.MAX_VALUE) {
				logger.info("File is too large");
			}

			while (numRead >= 0) {
				numRead = is.read(bytes, offset, bytes.length - offset);
				offset += numRead;
			}
			if (offset < bytes.length) {
				logger.error("Could not complete reading WAR file:"
						+ file.getName());
			}
			is.close();

		} catch (Exception e) {
			logger.error("Cannot find the corresponding WAR file." + e);
		}
		return bytes;
	}
}
