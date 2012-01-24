package org.dynasoar.comm;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.apache.log4j.Logger;
import org.dynasoar.config.Configuration;
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
	private static volatile Thread th = null;
	private JmDNS jmDNS = null;

	private HashMap<String, InetAddress> nodes = null;
	private List<Event> events = null;

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

			// Initialize jmDNS for node discovery
			jmDNS = JmDNS.create(address);
			logger.debug("Address: " + address + " :: Interface: "
					+ jmDNS.getInterface());

			java.util.logging.Logger logger = java.util.logging.Logger
					.getLogger(JmDNS.class.toString());
			ConsoleHandler handler = new ConsoleHandler();
			logger.addHandler(handler);
			logger.setLevel(Level.FINER);
			handler.setLevel(Level.FINER);

			// Start the mDNS listener
			jmDNS.addServiceListener("_dynasoar._http._tcp.local.",
					new DynasoarNodeListener());

			// Register self as a service
			ServiceInfo si = ServiceInfo.create("_dynasoar._http._tcp.local.",
					Configuration.getConfig("nodeName"), 3030,
					"DynaSOAr Service node");

			Thread.sleep(1000);
			jmDNS.registerService(si);
			logger.info("Service added");

		} catch (Exception e) {
			jmDNS.unregisterAllServices();
			logger.error("An Error occurred while registering service.", e);
		}

		try {
			// Initialize
			nodes = new HashMap<String, InetAddress>();
			events = new ArrayList<Event>();

			Thread thisThread = Thread.currentThread();
			while (thisThread == th) {
				// TODO: Make sure all nodes are in sync

				// Perhaps, calculate a hash of config files and make sure all
				// nodes has the same hash?
				// Or, maintain lastSync timestamp on disk?

				// Things to sync
				// * Service config files and WAR packages [Get a hash(?) from
				// ServiceMonitor]
				// * Service deploy info [Get from ServiceMonitor]
				// * Node loads [Get current Node's load from NodeMonitor.
				// Maintain list of loads of all nodes in Communicator]

				Thread.sleep(5000);
			}

			// Clean exit for the thread
			jmDNS.unregisterAllServices();
			logger.info("NodeCommunicator shutdown complete.");

		} catch (Exception e) {
			jmDNS.unregisterAllServices();
			logger.error("An Error occurred in communication loop.", e);
		}
	}

	public static void shutdown() {
		logger.info("Shutting Down NodeCommunicator");
		th = null;
	}

	public void addNode(String name, InetAddress address) {
		nodes.put(name, address);
	}

	public void removeNode(String name) {
		nodes.remove(name);
	}

	static class DynasoarNodeListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
			if (event.getInfo() != null) {
				logger.info("New DynaSOAr Service node added - "
						+ event.getInfo());
				logger.debug("InetAddress: " + event.getInfo().getInetAddress());

				// Add the node address to communicator
				NodeCommunicator.get().addNode(event.getInfo().getName(),
						event.getInfo().getInetAddress());
			}
		}

		public void serviceRemoved(ServiceEvent event) {
			if (event.getInfo() != null) {
				logger.info("DynaSOAr Service node removed - "
						+ event.getInfo().getName());

				NodeCommunicator.get().removeNode(event.getInfo().getName());
			}
		}

		public void serviceResolved(ServiceEvent event) {
			if (event.getInfo() != null) {
				logger.info("DynaSOAr Service node resolved - "
						+ event.getInfo().getName());
				logger.debug("InetAddress: " + event.getInfo().getInetAddress());

				// Add the node address to communicator
				NodeCommunicator.get().addNode(event.getInfo().getName(),
						event.getInfo().getInetAddress());
			}
		}
	}
}
