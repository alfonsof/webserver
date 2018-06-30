/**
 * ServerSettings: Contains the settings of the Web Server and read of configuration file
 * @author      Alfonso Fernandez-Barandiaran
 */

package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerSettings {
	
    /**
     * Port number for web server by default
     */
    public static final int SERVER_PORT_DEFAULT = 9090;
    
    /**
     * Document root directory by default
     */
    public static final String DOCUMENT_ROOT_DEFAULT = "wwwdocs/";
    
    /**
     * Directory listing allowed by default
     */
    public static final boolean DIRECTORY_LISTING = true;
    
    /**
     * Threads number by default
     */
    public static final int N_THREADS_DEFAULT = 10;
    
    /**
     * Max buffer size for a file by default
     */
    public static final int FILE_BUFFER_SIZE = 65536;

    /**
     * The socket timeout when waiting for line request by default
     */
    public static final int REQUEST_READ_TIMEOUT = 5000;

    /**
     * The socket timeout when waiting for headers by default
     */
    public static final int HEADER_READ_TIMEOUT = 2000;

    /**
     * Config file properties names for Server port number
     */
    public static final String SERVER_PORT_PROP_NAME = "ServerPort";

    /**
     * Config file properties names for document root directory
     */
    public static final String DOCUMENT_ROOT_PROP_NAME = "DocumentRoot";

    /**
     * Config file properties names for directory listing allowed
     */
    public static final String DIRECTORY_LISTING_PROP_NAME = "DirectoryListing";
    
    /**
     * Config file properties names for threads number in Web Server
     */
    public static final String N_THREADS_PROP_NAME = "ThreadsNumber";
    
    /**
     * Status Code 200: OK
     */
    public static final String HTTP_STR_OK = "200 OK";
    
    /**
     * Status Code 400: Client error - Bad Request
     */
    public static final String HTTP_STR_BAD_REQUEST = "400 Bad Request";
    
    /**
     * Status Code 404: Client error - Not Found
     */
    public static final String HTTP_STR_NOT_FOUND = "404 Not Found";
    
    /**
     * Status Code 408: Client error - Request Time-out
     */
    public static final String HTTP_STR_REQUEST_TIMEOUT = "408 Request Time-out";
    
    /**
     * Status Code 500: Server error - Internal Server Error
     */
    public static final String HTTP_STR_SERVER_ERROR = "500 Internal Server Error";

    /**
     * Status Code 501: Server error - Not Implemented
     */
    public static final String HTTP_STR_NOT_IMPLEMENTED = "501 Not Implemented";
    
    // Message for GetProperty
    private static final String GETPROPERTY_MESSAGE = "getProperty: ";
    
    // Logger
    private static final Logger logger = LogManager.getLogger(ServerSettings.class.getName());

    // Port number for web server
    private int serverPort = SERVER_PORT_DEFAULT;
    
    // Document root directory
    private String documentRoot = DOCUMENT_ROOT_DEFAULT;
    
    // Directory listing allowed
    private boolean directoryListing = DIRECTORY_LISTING;

    // Number of threads in web server
    private int nThreads = N_THREADS_DEFAULT;
	
    // Max buffer size for a file
    private int fileBufferSize = FILE_BUFFER_SIZE;

    // The socket timeout when waiting for line request
    private int requestReadTimeout = REQUEST_READ_TIMEOUT;

    // The socket timeout when waiting for headers
    private int headerReadTimeout = HEADER_READ_TIMEOUT;

    /**
     * Class constructor
     */
    public ServerSettings(boolean readConfigFile) {
    	if (readConfigFile) {
    		readConfig();
    	}
    }
    
    /**
     * Get Port number for web server
     * @return int
     */
    public int getServerPort() {
    	return serverPort;
    }
    
    /**
     * Get Document root directory
     * @return String
     */
    public String getDocumentRoot() {
    	return documentRoot;
    }
    
    /**
     * Get Directory listing allowed
     * @return boolean
     */
    public boolean getDirectoryListing() {
    	return directoryListing;
    }

    /**
     * Get Number of threads in web server
     * @return int
     */
    public int getNThreads() {
    	return nThreads;
    }
	
    /**
     * Get Max buffer size for a file
     * @return int
     */
    public int getFileBufferSize() {
    	return fileBufferSize;
    }

    /**
     * Get The socket timeout when waiting for line request
     * @return int
     */
    public int getRequestReadTimeout() {
    	return requestReadTimeout;
    }

    /**
     * Get The socket timeout when waiting for headers
     * @return int
     */
    public int getHeaderReadTimeout() {
    	return headerReadTimeout;
    }

    // Read properties file for configuring the Web Server
    private void readConfig() {
    	Properties props = new Properties();
        InputStream is = null;

        logger.trace("Read Config");
		try { 
			is = ServerSettings.class.getClassLoader().getResourceAsStream("webserver.properties");

			// load properties from properties file
			if (is != null) {
	        	logger.info("Config file found");

				props.load(is);
        	
				// read properties
				if (props.containsKey(SERVER_PORT_PROP_NAME)) {
					serverPort = Integer.parseInt(props.getProperty(SERVER_PORT_PROP_NAME));
					logger.trace(GETPROPERTY_MESSAGE + SERVER_PORT_PROP_NAME);
				}
				if (props.containsKey(DOCUMENT_ROOT_PROP_NAME)) {
					documentRoot = props.getProperty(DOCUMENT_ROOT_PROP_NAME);
					logger.trace(GETPROPERTY_MESSAGE + DOCUMENT_ROOT_PROP_NAME);
				}
				if (props.containsKey(DIRECTORY_LISTING_PROP_NAME)) {
					directoryListing = getDirectoryListing(props.getProperty(DIRECTORY_LISTING_PROP_NAME));
					logger.trace(GETPROPERTY_MESSAGE + DIRECTORY_LISTING_PROP_NAME);
				}
				if (props.containsKey(N_THREADS_PROP_NAME)) {
					nThreads = Integer.parseInt(props.getProperty(N_THREADS_PROP_NAME));
					logger.trace(GETPROPERTY_MESSAGE + N_THREADS_PROP_NAME);
				}
			} else {
        		logger.info("Config file not found");
        	}
		} catch (IOException e) {
			logger.info("Config file not found", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Config file error", e);
				}
			}
		}
		logger.trace("Server Port: " + serverPort);
		logger.trace("Document Root: " + documentRoot);
		logger.trace("Directory Listing: " + directoryListing);
		logger.trace("Threads number: " + nThreads);
		logger.trace("File Buffer Size: " + fileBufferSize);
		logger.trace("Request Read Timeout: " + requestReadTimeout);
		logger.trace("Header Read Timeout: " + headerReadTimeout);
    }

    // Put Directory Listing value
    private boolean getDirectoryListing(String value) {
    	boolean dirListing = false;
    	
		if ("y".equalsIgnoreCase(value)) {
			dirListing = true;
		}
		
		return dirListing;
    }
}
