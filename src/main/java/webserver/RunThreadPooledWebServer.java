package webserver;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class runs the Web Server
 * @author      Alfonso Fernandez-Barandiaran
 * @version     1.1
 * @since       2016-12-04
 */

public class RunThreadPooledWebServer {
	private static final Logger logger = LogManager.getLogger(RunThreadPooledWebServer.class.getName());
	
	private RunThreadPooledWebServer() {
	    throw new IllegalAccessError("Utility class");
	}

	/**
     * Runs Web Server Application
     * @param args	No used
     */
	public static void main(String[] args) {
		// Load configuration
		ServerSettings serverSettings = new ServerSettings();
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
	
}
