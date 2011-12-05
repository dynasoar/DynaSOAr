package org.dynasoar;

import org.dynasoar.config.Configuration;
import org.dynasoar.monitor.NodeMonitor;

/**
 * Initializes and starts up the application.
 * 
 * @author Rakshit Menpara
 */
public class Bootstrap {
	public static void main(String args[]) {
		// Read configuration file
		Configuration.readConfiguration();

		// Start up NodeMonitor
		NodeMonitor.start();
	}
}
