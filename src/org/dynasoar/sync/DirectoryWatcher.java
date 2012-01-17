package org.dynasoar.sync;

/**
 * Watches a directory specified in DynaSOAr configuration for changes
 *
 * @author Sagar Virani
 */
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.log4j.Logger;
import org.dynasoar.config.Configuration;
import org.dynasoar.service.ServiceMonitor;

// TODO: Write clean exit
public class DirectoryWatcher {

	private static Logger logger = Logger.getLogger(DirectoryWatcher.class);
	private ChangeEvent changeEvent = null;

	public DirectoryWatcher(ChangeEvent event) {
		this.changeEvent = event;
	}

	public void watch(String dirPath) {

		// TODO: Move this to bootstrap
		Path dirToMonitor = Paths.get(dirPath);

		try {
			WatchService watcher = dirToMonitor.getFileSystem()
					.newWatchService();

			// Register create, modify and delete watchers
			dirToMonitor.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);

			while (true) {
				// Retrieve key
				WatchKey key = watcher.take();

				// Process events
				for (WatchEvent<?> event : key.pollEvents()) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						logger.info("Created: " + event.context().toString());
						changeEvent.fileCreated(event.context().toString());
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						logger.info("Delete: " + event.context().toString());
						changeEvent.fileRemoved(event.context().toString());
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						logger.info("Modify: " + event.context().toString());
						changeEvent.fileModified(event.context().toString());
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
}
