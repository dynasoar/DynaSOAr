package org.dynasoar.service;

import org.apache.log4j.Logger;
import org.dynasoar.sync.DirectoryWatcher;
import org.dynasoar.config.Configuration;
import java.util.*;

/**
 * ServiceMonitor is responsible for monitoring changes in Service config files.
 * It is supposed to act on and notify NodeCommunicator of any change in
 * Service.
 * 
 * @author Rakshit Menpara
 */
public class ServiceMonitor implements Runnable {

    private static ServiceMonitor current = null;
    private static Logger logger = Logger.getLogger(ServiceMonitor.class);
    private static Thread th = null;
    // Don't do this. Use constructors.
    DirectoryWatcher dir = new DirectoryWatcher();

    static class Bean {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    private static HashMap<Bean, String> service = null;

    public static class ServiceTracker {

        public void serviceAdded(String serviceName) {
            System.out.println("Service affected = " + serviceName);
            Bean b = new Bean();
            b.setName(serviceName);
            service.put(b, serviceName);
        }

        public void serviceDeleted(String serviceName) {
            System.out.println("Service affected = " + serviceName);
            Bean b = new Bean();
            b.setName(serviceName);
            service.put(b, serviceName);
        }

        public void serviceModified(String serviceName) {
            System.out.println("Service affected = " + serviceName);
            Bean b = new Bean();
            b.setName(serviceName);
            service.put(b, serviceName);

        }
    }

    public static void start() {
        // TODO: Start this in a separate thread
        current = new ServiceMonitor();
        th = new Thread(current, "ServiceMonitor");
        th.start();
    }

    public static boolean isRunning() {
        if (current == null) {
            return false;
        }

        return th.isAlive();
    }

    @Override
    public void run() {


        // Read "ServiceConfigDir" from configuration and starts listening
        // to the directory
        String path = Configuration.getConfig("serviceConfigDir");



        // while (true) {
        dir.watch(path, new ServiceTracker());
        // }
        // In case of any changes in directory, Read service config file,
        // load/re-deploy the service on local server

        // Notify NodeCommunicator of all the changes occurred

    }
}
