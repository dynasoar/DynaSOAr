package org.dynasoar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/*
 * Reads and maintains list of configuration settings in memory.
 */
public class Configuration {

	private static Logger logger = Logger.getLogger(Configuration.class);
	private static HashMap<String, String> config = null;

	/**
	 * Reads configuration from default file. TODO: Replace it with a config
	 * file watcher
	 */
	public static void readConfiguration() {
		// TODO: Change path accordingly. Use relative path.
		String path = "config.json";
		readConfiguration(path);
	}

	/**
	 * Reads configuration from specified config file and loads it into memory.
	 * TODO: Replace it with a config file watcher
	 * 
	 * @param path
	 */
	public static void readConfiguration(String path) {

		// Parse the file content using Jackson
		ObjectMapper mapper = new ObjectMapper();
		try {
			logger.info("Reading configuration...");
			File tmpFile = new File(path);
			// logger.info("File Exists: " +
			// tmpFile.getAbsoluteFile().getPath());

			config = (HashMap<String, String>) mapper.readValue(tmpFile,
					Map.class);
			logger.info("SUCCESS");
		} catch (Exception e) {
			logger.error("Configuration Parsing Failed.", e);
		}
	}

	/**
	 * Get value of a specified configuration string. Returns null if config
	 * setting does not exist.
	 * 
	 * @param name
	 * @return
	 */
	public static String getConfig(String name) {
		// TODO: Check if the config hashmap is loaded first
		System.out.println("Name: " + config.containsValue("serviceConfigDir"));
		if (config.isEmpty()) {
			return null;
		} else {
			return config.get(name);
		}
	}

	/**
	 * Sets value of a specified configuration string.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public static void setConfig(String name, String value) {
	}
}
