package webserver;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application class that boots and runs the thread-pooled web server.
 *
 * @author Alfonso Fernandez-Barandiaran
 */
public class RunThreadPooledWebServer {
    private static final Logger logger = LogManager.getLogger(RunThreadPooledWebServer.class.getName());

    /**
     * Main entry point for the web server application. Loads configuration,
     * validates the document root directory, and starts the thread-pooled web server.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Load configuration
        ServerSettings serverSettings = new ServerSettings(true);
        // Test if exist Document Root
        File documentDir = new File(serverSettings.getDocumentRoot());
        if (!documentDir.exists() && !documentDir.isDirectory()) {
           logger.error("No exists Document Root [" + documentDir.getAbsolutePath()+ "]");
           logger.error("Web Server Stopped");
           System.exit(-1);
        }
        // Run Web Server
        new ThreadPooledWebServer(serverSettings).run();
    }

    /**
     * Private constructor to prevent instantiation of this utility/main class.
     *
     * @throws IllegalAccessError if invoked via reflection
     */
    private RunThreadPooledWebServer() {
        throw new IllegalAccessError("Utility class");
    }
}
