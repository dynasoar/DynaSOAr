package org.dynasoar.communicator;

import org.apache.log4j.Logger;
import org.dynasoar.queueListner.Listner;

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
        while (!shutdown) {
            // TODO: Implement all the TCP comm stuff
        }
        while (true) {
            try {
                Listner.message_Receiver("msgProducer");
            } catch (Exception e) {
            }
        }
    }

    public void shutdown() {
        logger.info("Shutting Down NodeCommunicator");
        shutdown = true;
    }
}
