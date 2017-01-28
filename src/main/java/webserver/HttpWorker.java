package webserver;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements the worker in order to manage every http connection 
 * @author      Alfonso Fernandez-Barandiaran
 * @version     1.1
 * @since       2016-12-04
 */

public class HttpWorker implements Runnable {

	private static final Logger logger = LogManager.getLogger(HttpWorker.class.getName());
	private ServerSettings serverSettings;
    protected Socket clientSocket = null;

    /**
     * Class constructor
     * @param serverSettings	Settings of the Web Server
     * @param clientSocket		Socket of a client
     */
    public HttpWorker(ServerSettings serverSettings, Socket clientSocket) {
    	this.serverSettings = serverSettings;
        this.clientSocket = clientSocket;
    }

    /**
     * Run a instance of the Web Server
     */
    @Override
    public void run() {
    	try {
    		HttpHandler httpHandler = new HttpHandler(serverSettings, clientSocket);
    		httpHandler.handleConnection();
        } catch (IOException e) {
        	logger.error("HttpWorker: ", e);
        }
    }
}
