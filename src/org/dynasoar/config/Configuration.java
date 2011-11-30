package org.dynasoar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class Configuration {
	private static Logger logger = Logger.getLogger(Configuration.class);

	public static HashMap<String, String> readConfiguration() {
		// TODO: Change path accordingly. Use relative path.
		String path = "config.json";
		return readConfiguration(path);
	}

	public static HashMap<String, String> readConfiguration(String path) {
		HashMap<String, String> configMap = null;

		// Parse the file content using Jackson
		ObjectMapper mapper = new ObjectMapper();
		try {
			configMap = (HashMap<String, String>) mapper.readValue(new File(
					"user.json"), Map.class);
		} catch (Exception e) {
			logger.error("Configuration Parsing Failed.", e);
		}

		return configMap;
	}
}