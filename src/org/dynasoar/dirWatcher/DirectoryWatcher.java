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
import org.dynasoar.config.Configuration;
import org.dynasoar.service.ServiceMonitor;

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
                StringBuilder message = new StringBuilder();
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    //logger.info("Created: " + event.context().toString());
                    message.append(event.context().toString())
                            .append(" " + "Created" + " ")
                            .append(Configuration.getConfig("serviceConfigDir"))
                            .append(" ")
                            .append(Configuration.getConfig("WARfileDir"));
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    //logger.info("Delete: " + event.context().toString());
                    message.append(event.context().toString())
                            .append(" " + "Deleted" + " ")
                            .append(Configuration.getConfig("serviceConfigDir"))
                            .append(" ")
                            .append(Configuration.getConfig("WARfileDir"));
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    //logger.info("Modify: " + event.context().toString());
                    message.append(event.context().toString())
                            .append(" " + "Modified" + " ")
                            .append(Configuration.getConfig("serviceConfigDir"))
                            .append(" ")
                            .append(Configuration.getConfig("WARfileDir"));
                }
                ServiceMonitor.message_Producer(message.toString());
            }
        } catch (Exception e) {
            logger.error("An error occurred while watching directory: " + dirPath, e);
        }
    }
}
