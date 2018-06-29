package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Manages the thread pooled web server
 * @author      Alfonso Fernandez-Barandiaran
 */


public class ThreadPooledWebServer implements Runnable {
	private static final Logger logger = LogManager.getLogger(ThreadPooledWebServer.class.getName());
	private ServerSettings 	serverSettings;
    private int          	serverPort    = 9090;
    private ServerSocket 	serverSocket  = null;
    private boolean      	isStopped     = false;
    private ExecutorService threadPool;

    /**
     * Class constructor
     * @param serverSettings	Settings of the Web Server
     */
    public ThreadPooledWebServer(ServerSettings serverSettings) {
    	this.serverSettings = serverSettings;
        this.serverPort = serverSettings.getServerPort();
        this.threadPool = Executors.newFixedThreadPool(serverSettings.getNThreads());
    }

    /**
     * Runs Web Server
     */
    @Override
    public void run() {
        Thread       	runningThread = null;
        
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
     * Stops Web Server
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

    private void openServerSocket() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
        	logger.error("Cannot open port: " + serverPort, e);
        	throw new WebServerException("Cannot open port: " + serverPort, e);
        }
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

}
