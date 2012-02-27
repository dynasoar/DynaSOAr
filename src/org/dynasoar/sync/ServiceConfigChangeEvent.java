package org.dynasoar.sync;

import java.io.File;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.dynasoar.comm.NodeCommunicator;
import org.dynasoar.service.DynasoarService;
import org.dynasoar.service.ServiceChangeEvent;
import org.dynasoar.service.ServiceEventType;

/**
 * Implements ChangeEvent interface, which will handle directory change events
 * of Service Config Directory
 * 
 * @author Rakshit Menpara
 */
public class ServiceConfigChangeEvent {
	private static Logger logger = Logger
			.getLogger(ServiceConfigChangeEvent.class);

	public void fileCreated(String path) {
		DynasoarService service = this.readServiceConfig(path);

		if (service != null) {
			NodeCommunicator.newEvent(new ServiceChangeEvent(service,
					ServiceEventType.ADDED));
			logger.info("Service added - " + service.getName());
		} else {
			logger.info("Service Config File Null");
		}
	}

	public void fileModified(String path) {
		DynasoarService service = this.readServiceConfig(path);

		if (service != null) {
			logger.info("Service changed - " + service.getName());
			NodeCommunicator.newEvent(new ServiceChangeEvent(service,
					ServiceEventType.CHANGED));
		} else {
			logger.info("Service Config File Null");
		}
	}

	public void fileRemoved(String path) {
		// TODO: Correct
		DynasoarService service = this.readServiceConfig(path);

		if (service != null) {
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
			service.setShortName(configFile.getName().split("\\.")[0]);
		} catch (Exception e) {
			logger.error("ServiceConfig parsing failed.", e);
		}

		return service;
	}
}
