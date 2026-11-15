package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the configuration settings of the web server and handles
 * reading properties from the configuration file.
 *
 * @author Alfonso Fernandez-Barandiaran
 */
public class ServerSettings {

    /** Port number for web server by default. */
    public static final int SERVER_PORT_DEFAULT = 9090;

    /** Document root directory by default. */
    public static final String DOCUMENT_ROOT_DEFAULT = "wwwdocs/";
    
    /** Directory listing allowed by default. */
    public static final boolean DIRECTORY_LISTING = true;
    
    /** Threads number by default. */
    public static final int N_THREADS_DEFAULT = 10;
    
    /** Max buffer size for a file by default. */
    public static final int FILE_BUFFER_SIZE = 65536;

    /** The socket timeout when waiting for line request by default. */
    public static final int REQUEST_READ_TIMEOUT = 5000;

    /** The socket timeout when waiting for headers by default. */
    public static final int HEADER_READ_TIMEOUT = 2000;

    /** Config file properties names for Server port number. */
    public static final String SERVER_PORT_PROP_NAME = "ServerPort";

    /** Config file properties names for document root directory. */
    public static final String DOCUMENT_ROOT_PROP_NAME = "DocumentRoot";

    /** Config file properties names for directory listing allowed. */
    public static final String DIRECTORY_LISTING_PROP_NAME = "DirectoryListing";
    
    /** Config file properties names for threads number in Web Server. */
    public static final String N_THREADS_PROP_NAME = "ThreadsNumber";
    
    /** Status Code 200: OK */
    public static final String HTTP_STR_OK = "200 OK";
    
    /** Status Code 400: Client error - Bad Request */
    public static final String HTTP_STR_BAD_REQUEST = "400 Bad Request";
    
    /** Status Code 404: Client error - Not Found */
    public static final String HTTP_STR_NOT_FOUND = "404 Not Found";
    
    /** Status Code 408: Client error - Request Time-out */
    public static final String HTTP_STR_REQUEST_TIMEOUT = "408 Request Time-out";
    
    /** Status Code 500: Server error - Internal Server Error */
    public static final String HTTP_STR_SERVER_ERROR = "500 Internal Server Error";

    /** Status Code 501: Server error - Not Implemented */
    public static final String HTTP_STR_NOT_IMPLEMENTED = "501 Not Implemented";
    
    private static final String GETPROPERTY_MESSAGE = "getProperty: ";
    private static final Logger logger = LogManager.getLogger(ServerSettings.class.getName());

    private int serverPort = SERVER_PORT_DEFAULT;
    private String documentRoot = DOCUMENT_ROOT_DEFAULT;
    private boolean directoryListing = DIRECTORY_LISTING;
    private int nThreads = N_THREADS_DEFAULT;
    private int fileBufferSize = FILE_BUFFER_SIZE;
    private int requestReadTimeout = REQUEST_READ_TIMEOUT;
    private int headerReadTimeout = HEADER_READ_TIMEOUT;

    /**
     * Constructs a new {@code ServerSettings} instance.
     *
     * @param readConfigFile {@code true} to read configuration from the properties file;
     *                       {@code false} to use default settings
     */
    public ServerSettings(boolean readConfigFile) {
        if (readConfigFile) {
            readConfig();
        }
    }

    /**
     * Returns the port number for the web server.
     *
     * @return the server port number
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Returns the document root directory path.
     *
     * @return the document root string
     */
    public String getDocumentRoot() {
        return documentRoot;
    }

    /**
     * Returns whether directory listing is allowed.
     *
     * @return {@code true} if directory listing is enabled; {@code false} otherwise
     */
    public boolean getDirectoryListing() {
        return directoryListing;
    }

    /**
     * Returns the number of worker threads configured for the web server.
     *
     * @return the number of threads
     */
    public int getNThreads() {
        return nThreads;
    }

    /**
     * Returns the max buffer size for reading files.
     *
     * @return the file buffer size in bytes
     */
    public int getFileBufferSize() {
        return fileBufferSize;
    }

    /**
     * Returns the socket timeout when waiting for the request line.
     *
     * @return the request read timeout in milliseconds
     */
    public int getRequestReadTimeout() {
        return requestReadTimeout;
    }

    /**
     * Returns the socket timeout when waiting for request headers.
     *
     * @return the header read timeout in milliseconds
     */
    public int getHeaderReadTimeout() {
        return headerReadTimeout;
    }

    /**
     * Reads the properties file (`webserver.properties`) from the classpath
     * to configure the web server parameters.
     */
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

    /**
     * Parses the directory listing property value string into a boolean.
     *
     * @param value the property string value from the configuration file
     * @return {@code true} if the value equals "y" (case-insensitive); {@code false} otherwise
     */
    private boolean getDirectoryListing(String value) {
        boolean dirListing = false;
        
        if ("y".equalsIgnoreCase(value)) {
            dirListing = true;
        }
        
        return dirListing;
    }
}
