package webserver;

import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements a worker thread to manage individual HTTP connections concurrently.
 *
 * @author Alfonso Fernandez-Barandiaran
 */
public class HttpWorker implements Runnable {

    private static final Logger logger = LogManager.getLogger(HttpWorker.class.getName());
    private ServerSettings serverSettings;
    private Socket clientSocket = null;

    /**
     * Constructs a new {@code HttpWorker} instance.
     *
     * @param serverSettings settings of the web server
     * @param clientSocket   socket representing the client connection
     */
    public HttpWorker(ServerSettings serverSettings, Socket clientSocket) {
        this.serverSettings = serverSettings;
        this.clientSocket = clientSocket;
    }

    /**
     * Executes the worker task, initializing the {@link HttpHandler}
     * to process the client connection and handling any potential I/O errors.
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
