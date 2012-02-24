package org.dynasoar.sync;

/**
 * Watches a directory specified in DynaSOAr configuration for changes
 *
 * @author Sagar Virani
 */
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.log4j.Logger;

// TODO: Write clean exit
public class DirectoryWatcher implements Runnable {

	private static Logger logger = Logger.getLogger(DirectoryWatcher.class);
	private ServiceConfigChangeEvent changeEvent = null;
	private WatchService watcher = null;
        private static DirectoryWatcher current = null;
	private static Thread th = null;
        private static String dirPath = null;
        
        public void start(String dirPath) {
            
		this.dirPath = dirPath;
		th = new Thread(this, "DirectoryWatcher");
		th.start();                
	}
       
	public DirectoryWatcher(ServiceConfigChangeEvent event) {
                this.changeEvent = event;
	}
        public void run() {
                    System.out.println("Path =" +dirPath);
                    watch(dirPath);
        }
	public void watch(String dirPath) {

		Path dirToMonitor = Paths.get(dirPath);

		try {
			watcher = dirToMonitor.getFileSystem().newWatchService();

			// Register create, modify and delete watchers
			dirToMonitor.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);

			while (watcher != null) {
				// Retrieve key
				WatchKey key = watcher.take();

				// Process events
				for (WatchEvent<?> event : key.pollEvents()) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						logger.info("Created: " + event.context().toString());
						changeEvent.fileCreated(dirPath + event.context().toString());
					} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						logger.info("Delete: " + event.context().toString());
						changeEvent.fileRemoved(dirPath + event.context().toString());
					} else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						logger.info("Modify: " + event.context().toString());
						changeEvent.fileModified(dirPath + event.context().toString());
					} else if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
						logger.error("WatchService overflow. Some changes might have been lost.");
						// TODO: Handle this to re-initialize the service
						// repository from serviceConfig
					}
				}

				// Reset the key
				boolean valid = key.reset();
				if (!valid) {
					logger.info("Invalid WatchKey. Exiting the loop.");
					break;
				} 
			}

		} catch (Exception e) {
			logger.error("An error occurred while watching directory: "
					+ dirPath, e);
		}
	}

	public void exit() {
		if (watcher != null) {
			try {
				logger.info("Removing watcher on serviceConfig.");
				watcher.close();
			} catch (IOException e) {
				logger.error("Clean exit failed", e);
			} finally {
				logger.info("ServiceConfig watcher removed.");
				watcher = null;
			}
		}
	}
}
