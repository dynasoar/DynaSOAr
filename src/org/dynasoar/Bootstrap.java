package org.dynasoar;

import org.dynasoar.config.Configuration;
import org.dynasoar.monitor.NodeMonitor;
import org.dynasoar.service.ServiceMonitor;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

/**
 * Initializes and starts up the application.
 * 
 * @author Rakshit Menpara
 */

public class Bootstrap {

	private static Logger log = Logger.getLogger(Bootstrap.class);

	public static void main(String args[]) {

		// Set up a simple configuration that logs on the console.
		BasicConfigurator.configure();

		// Check if configuration path is specified as a parameter
		if (args.length > 2) {
			String path = args[2];

			// Read configuration file
			Configuration.readConfiguration(path);
		} else {
			// Read configuration file
			Configuration.readConfiguration();
		}

		// Start up ServiceMonitor
		ServiceMonitor.start();

		// Start up NodeMonitor
		NodeMonitor.start();

	}
}
