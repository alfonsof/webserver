/**
 * Request: Manages the http request
 */

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
 */
public class Request {

	private static final Pattern HEADER_LINE_ACCEPTED = Pattern.compile("([^:]*):\\s*(.*)");
	private static final Logger logger = LogManager.getLogger(Request.class.getName());
	private ServerSettings serverSettings;
	private Socket clientSocket;
	private InputStream input;
	private BufferedReader reader;
	private String requestLine;
	
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
	public boolean readRequest() throws IOException {
		Map<String, String> requestHeaders;
		
		requestLine = readRequestLine();
	    if ((requestLine != null) && (!"".equals(this.requestLine))) {  // No empty requestLine
		    requestHeaders = readRequestHeaders(reader);
		    if (!requestHeaders.isEmpty()) {  // No empty requestHeaders
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
	
	private String readRequestLine() throws IOException {
    	String reqLine;
    	
       	clientSocket.setSoTimeout(serverSettings.getRequestReadTimeout());
       	reqLine = reader.readLine();
   	    logger.trace("Request line: " + reqLine);
	
   	    return reqLine;
    }
    
    private Map<String, String> readRequestHeaders(BufferedReader reader) throws IOException {
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
