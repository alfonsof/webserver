package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements a multithreaded web server using a fixed thread pool to manage
 * incoming client connections concurrently.
 *
 * @author Alfonso Fernandez-Barandiaran
 */
public class ThreadPooledWebServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(ThreadPooledWebServer.class.getName());
    private ServerSettings  serverSettings;
    private int             serverPort    = 9090;
    private ServerSocket    serverSocket  = null;
    private boolean         isStopped     = false;
    private ExecutorService threadPool;

    /**
     * Constructs a new {@code ThreadPooledWebServer} with the specified server settings.
     *
     * @param serverSettings configuration settings for the web server
     */
    public ThreadPooledWebServer(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
        this.serverPort = serverSettings.getServerPort();
        this.threadPool = Executors.newFixedThreadPool(serverSettings.getNThreads());
    }

    /**
     * Starts the web server main loop, opening the server socket and continuously accepting
     * incoming client connections, which are then dispatched to the worker thread pool.
     */
    @Override
    public void run() {
        Thread          runningThread = null;
        
        synchronized (this) {
            runningThread = Thread.currentThread();
            logger.trace("runningThread: " + runningThread);
        }
        openServerSocket();
        logger.info("WebServer running");
        
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    logger.error("Web Server Stopped");
                    return;
                }
                logger.error("Error accepting client connection", e);
                throw new WebServerException("Error accepting client connection", e);
            }
            threadPool.execute(new HttpWorker(serverSettings, clientSocket));
        }
        threadPool.shutdown();
        logger.info("Web Server Stopped");
    }

    /**
     * Stops the web server by setting the stopped flag and closing the server socket.
     */
    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error("Error closing Web Server", e);
            throw new WebServerException("Error closing Web Server", e);
        }
    }

    /**
     * Opens the server socket on the configured server port.
     *
     * @throws WebServerException if the server socket cannot be opened
     */
    private void openServerSocket() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            logger.error("Cannot open port: " + serverPort, e);
            throw new WebServerException("Cannot open port: " + serverPort, e);
        }
    }

    /**
     * Checks whether the web server has been requested to stop.
     *
     * @return {@code true} if the server is stopped; {@code false} otherwise
     */
    private synchronized boolean isStopped() {
        return isStopped;
    }
}
