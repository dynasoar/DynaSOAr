package org.dynasoar.communicator;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.apache.log4j.Logger;
import org.dynasoar.util.NetworkUtil;

/**
 * Takes care of communications between different nodes, along with maintaining
 * their list, services and load. Also responsible for communicating changes in
 * Service and synchronizing Service Applications (WAR files) across nodes.
 * 
 * @author Rakshit Menpara
 */
public class NodeCommunicator implements Runnable {

	private static NodeCommunicator current = null;
	private static Logger logger = Logger.getLogger(NodeCommunicator.class);
	private static Thread th = null;
	private boolean shutdown = false;
	private int pingInterval = 500;
	private JmDNS jmDNS = null;

	private HashMap<String, InetAddress> nodes = null;

	public static void start() {
		current = new NodeCommunicator();
		th = new Thread(current, "NodeCommunicator");
		th.start();
	}

	public static NodeCommunicator get() {
		if (current == null) {
			start();
		}
		return current;
	}

	public static boolean isRunning() {
		if (current == null) {
			return false;
		}

		return th.isAlive();
	}

	@Override
	public void run() {
		try {
			InetAddress address = NetworkUtil.getLocalNetworkAddress();

			if (address == null) {
				address = InetAddress.getLocalHost();
			}

			jmDNS = JmDNS.create(address);
			logger.debug("Address: " + address + " :: Interface: " + jmDNS.getInterface());
			
			java.util.logging.Logger logger = java.util.logging.Logger
					.getLogger(JmDNS.class.toString());
			ConsoleHandler handler = new ConsoleHandler();
			logger.addHandler(handler);
			logger.setLevel(Level.FINER);
			handler.setLevel(Level.FINER);

			// Start the mDNS listener
			jmDNS.addServiceListener("_http._tcp.local.",
					new DynasoarNodeListener());

			// Register self as a service
			ServiceInfo si = ServiceInfo.create("_http._tcp.local.",
					"dynasoar", 3030, "DynaSOAr Service");

			Thread.sleep(1000);
			jmDNS.registerService(si);
			logger.info("Service added");

			while (!shutdown) {
				// TODO: Implement all the TCP comm stuff
				//jmDNS.printServices();

				ServiceInfo[] services = jmDNS.list("_http._tcp.local.");

				logger.info(jmDNS.getInterface() + " :: Listed: "
						+ services.length);

				Thread.sleep(5000);
			}
		} catch (Exception e) {
			jmDNS.unregisterAllServices();
			e.printStackTrace();
		}
	}

	public void shutdown() {
		logger.info("Shutting Down NodeCommunicator");
		shutdown = true;
	}

	public void addNode(String name, InetAddress address) {
		nodes.put(name, address);
	}

	public void removeNode(String name) {
		nodes.remove(name);
	}

	static class DynasoarNodeListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
			logger.info("New HTTP Service added - " + event.getInfo());
			/*logger.info("New HTTP Service added - " + event.getInfo().getName());

			// Check if it is a valid DynaSOAr service
			if (event.getInfo().getName() == "dynasoar") {
				// Add the node address to communicator
				logger.info("Host Address: " + event.getInfo().getHostAddress());
				logger.info("Port: " + event.getInfo().getPort());
				logger.info("Qualified Name: "
						+ event.getInfo().getQualifiedName());
				logger.info("Server: " + event.getInfo().getServer());
				logger.info("URL: " + event.getInfo().getURL());
				logger.info("Address: " + event.getInfo().getAddress());
				logger.info("InetAddress: " + event.getInfo().getInetAddress());
			}*/
		}

		public void serviceRemoved(ServiceEvent event) {
			logger.info("New HTTP Service added - " + event.getInfo().getName());

			// Check if it is a valid DynaSOAr service
			if (event.getInfo().getName() == "dynasoar") {
				// Add the node address to communicator
				logger.info("Host Address: " + event.getInfo().getHostAddress());
				logger.info("Port: " + event.getInfo().getPort());
				logger.info("Qualified Name: "
						+ event.getInfo().getQualifiedName());
				logger.info("Server: " + event.getInfo().getServer());
				logger.info("URL: " + event.getInfo().getURL());
				logger.info("Address: " + event.getInfo().getAddress());
				logger.info("InetAddress: " + event.getInfo().getInetAddress());
			}
		}

		public void serviceResolved(ServiceEvent event) {
			logger.info("New HTTP Service added - " + event.getInfo().getName());

			// Check if it is a valid DynaSOAr service
			if (event.getInfo().getName() == "dynasoar") {
				// Add the node address to communicator
				logger.info("Host Address: " + event.getInfo().getHostAddress());
				logger.info("Port: " + event.getInfo().getPort());
				logger.info("Qualified Name: "
						+ event.getInfo().getQualifiedName());
				logger.info("Server: " + event.getInfo().getServer());
				logger.info("URL: " + event.getInfo().getURL());
				logger.info("Address: " + event.getInfo().getAddress());
				logger.info("InetAddress: " + event.getInfo().getInetAddress());
			}
		}
	}

}
