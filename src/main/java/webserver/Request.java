package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the http request
 * @author      Alfonso Fernandez-Barandiaran
 * @version     1.1
 * @since       2016-12-04
 */

public class Request {

	private static final Pattern HEADER_LINE_ACCEPTED = Pattern.compile("([^:]*):\\s*(.*)");
	private static final Logger logger = LogManager.getLogger(Request.class.getName());
	private ServerSettings serverSettings;
	private Socket clientSocket;
	private InputStream input;
	private BufferedReader reader;
	private String requestLine;
	private Map<String, String> headersRequest;
	
    /**
     * Class constructor
     * @param serverSettings	Settings of the Web Server
     * @param clientSocket		Socket of a client
     * @param input				Input of the request
     * @param reader			Buffer for the request
     */
	public Request(ServerSettings serverSettings, Socket clientSocket, InputStream input, BufferedReader reader) {
		this.serverSettings = serverSettings;
		this.clientSocket = clientSocket;
		this.input = input;
		this.reader = reader;
		logger.trace("Request->clientSocket: " + this.clientSocket);
		logger.trace("Request->input: " + this.input);
	}

    /**
     * Reads the http request
     * @throws IOException If an input or output 
     *                     exception occurred
     * @return boolean
     */
	public boolean read() throws IOException {
		this.requestLine = readRequestLine();
	    if ((this.requestLine != null) && (!"".equals(this.requestLine))) {  // No empty requestLine
		    this.headersRequest = readHeaders(reader);
		    if (this.headersRequest != null) {  // No empty headersRequest
		    	return true;
		    }
	    }
	    // Some is null
		return false;
	}

	/**
     * Reads the line of the request
     * @return String
     */
	public String getRequestLine() {
		return this.requestLine;
	}
	
    /**
     * Read the head of the request
     * @return Map(String, String)
     */
	public Map<String, String> getHeadersRequest() {
		return this.headersRequest;
	}

	private String readRequestLine() throws IOException {
    	String reqLine;
    	
       	clientSocket.setSoTimeout(serverSettings.getRequestReadTimeout());
       	reqLine = reader.readLine();
   	    logger.trace("Request line: " + reqLine);
	
   	    return reqLine;
    }
    
    private Map<String, String> readHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        int length;
        
        clientSocket.setSoTimeout(serverSettings.getHeaderReadTimeout());
        do {
            String headerLine = reader.readLine();
            logger.trace("Request Header line: " + headerLine);
            length = headerLine.length();
            logger.trace("Header line Lenght: " + length);
            if (length > 0) {
            	Matcher matcher = HEADER_LINE_ACCEPTED.matcher(headerLine);
            	if (matcher.matches()) {
            		headers.put(matcher.group(1), matcher.group(2).toLowerCase());
            	} else {
            		logger.info("Skipping invalid header: " + headerLine);
            	}
            }
        } while (length > 0);

        return headers;
    }

}
