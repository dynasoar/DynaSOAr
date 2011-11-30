package org.dynasoar;

import java.util.HashMap;

import org.dynasoar.config.Configuration;

public class Bootstrap {
	public static HashMap<String, String> config = null;
	public static void main(String args[]) {
		// Read configuration file
		config = Configuration.readConfiguration();
	}
}
