package org.dynasoar.comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.apache.log4j.Logger;
import org.dynasoar.config.Configuration;
import org.dynasoar.util.Event;
import org.dynasoar.util.NetworkUtil;

/**
 * Takes care of communications between different nodes, along with maintaining
 * their list, services and load. Also responsible for communicating changes in
 * Service and synchronizing Service Applications (WAR files) across nodes.
 * 
 * @author Rakshit Menpara
 */
public class NodeCommunicator extends EventHandler implements Runnable {

	private static NodeCommunicator current = null;
	private static Logger logger = Logger.getLogger(NodeCommunicator.class);
	private static volatile Thread th = null;
	private JmDNS jmDNS = null;

	private HashMap<String, InetAddress> nodes = null;

	public NodeCommunicator() {
		super(CommEvent.class);
	}

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

			Thread thisThread = Thread.currentThread();
			while (thisThread == th) {
				// Process all the events
				this.processEvents();

				// Wait till any other event is added
				//logger.info("Waiting till a new event is added.");
				//super.wait();
				//logger.info("Resuming operations.");

				// TODO: Start node comm listener
			//	ServerSocket listener = new ServerSocket(Integer
			//			.parseInt(Configuration.getConfig("commPort")));
				
				/*Event event = this.receiveEvent(listener);

				if (event != null) {
					newEvent(event);
				}*/
                                
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

	public static void newEvent(Event event) {
		if (current != null) {
			current.emitEvent(event);
		}
	}

	public void addNode(String name, InetAddress address) {
		nodes.put(name, address);
	}

	public void removeNode(String name) {
		nodes.remove(name);
	}

	/**
	 * {@link EventHandler#handle(Event)} implementation.
	 */
	@Override
	public void handle(Event event) {
		// TODO: Take action if necessary

		// Distribute the event to all the available nodes
		Iterator<String> i = nodes.keySet().iterator();
		while (i.hasNext()) {
			InetAddress nodeAddress = nodes.get(i.next());
			try {
				this.sendEvent(event, nodeAddress);
			} catch (IOException e) {
				logger.error("Error sending event to remote nodes", e);
			}
		}
	}

	private void sendEvent(Event event, InetAddress node) throws IOException {
		// TODO: This could be optimized using a socket pool
		Socket s = new Socket(node, Integer.parseInt(Configuration
				.getConfig("commPort")));

		ObjectOutputStream oout = new ObjectOutputStream(s.getOutputStream());
		oout.writeObject(event);
		oout.close();
		s.close();
	}

	private Event receiveEvent(Socket s) throws IOException {
		
                ObjectInputStream oin = new ObjectInputStream(s.getInputStream());
		Event event = null;
		try {
			event = (Event) oin.readObject();
		} catch (ClassCastException e) {
			logger.error("Non-event object received.", e);
		} catch (ClassNotFoundException e2) {
			logger.error("Unresolvable object type received.", e2);
		}

		return event;
	}

	static class DynasoarNodeListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
			if (event.getInfo() != null && !event.getInfo().getInetAddress().isLoopbackAddress() && !event.getInfo().getInetAddress().isSiteLocalAddress()) {
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
