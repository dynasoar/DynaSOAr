package org.dynasoar.dirWatcher;
/**
 *
 * @author sagar
 */
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import org.apache.log4j.Logger;
import org.dynasoar.Bootstrap;

public class DirectoryWatcher {
    private static Logger logger = Logger.getLogger(DirectoryWatcher.class);

    /**
     * @param args the command line arguments
     */
    public void watch(String dirPath) {
        
        // TODO: Move this to bootstrap
        Path dirToMonitor = Paths.get(dirPath);

        try {
            WatchService watcher = dirToMonitor.getFileSystem().newWatchService();
            dirToMonitor.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            WatchKey watckKey = watcher.take();
            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    logger.info("Created: " + event.context().toString());
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    logger.info("Delete: " + event.context().toString());
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    logger.info("Modify: " + event.context().toString());
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while watching directory: " + dirPath, e);
        }
    }
}
