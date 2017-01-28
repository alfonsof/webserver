package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the http request and response
 * @author      Alfonso Fernandez-Barandiaran
 * @version     1.1
 * @since       2016-12-04
 */

public class HttpHandler {
	private static final Logger logger = LogManager.getLogger(HttpHandler.class.getName());
	private ServerSettings serverSettings;
	private Socket clientSocket = null;

    /**
     * Class constructor
     * @param serverSettings	Settings of the Web Server
     * @param clientSocket		Socket of a client
     */
	public HttpHandler(ServerSettings serverSettings, Socket clientSocket) {
		this.serverSettings = serverSettings;
		this.clientSocket = clientSocket;
	}
	
    /**
     * Handles the connection
     * @throws IOException If an input or output 
     *                     exception occurred
     */
	public void handleConnection() throws IOException {
   	    InputStream input  = clientSocket.getInputStream();
   	    final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
   	    OutputStream output = clientSocket.getOutputStream();
   	    final Writer writer = new OutputStreamWriter(output);
    	Request request = new Request(serverSettings, clientSocket, input, reader);
    	Response response = new Response(serverSettings, request, output, writer);
   	    
   	    try {
   	    	logger.trace("read()");
   	    	if (request.read()) {
   	   	    	logger.trace("write()");
   	   	    	response.write();
   	    	} else { // Wrong message in read()
   	   	    	response.httpError(writer, "", ServerSettings.HTTP_STR_REQUEST_TIMEOUT, ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
   	   	    	writer.flush();
   	   	    	logger.info(ServerSettings.HTTP_STR_BAD_REQUEST);
   	    	}
   	    } catch (SocketTimeoutException e) {
   	    	response.httpError(writer, "", ServerSettings.HTTP_STR_REQUEST_TIMEOUT, ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
   	    	writer.flush();
   	    	logger.info(ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
   	    	logger.info("HttpHandler 1: ", e);
   	    } catch (SocketException e) {
   	    	response.httpError(writer, "", ServerSettings.HTTP_STR_SERVER_ERROR, ServerSettings.HTTP_STR_SERVER_ERROR);
   	    	writer.flush();
   	    	logger.info(ServerSettings.HTTP_STR_SERVER_ERROR);
   	    	logger.info("HttpHandler 2: ", e);
   	    }

        writer.close();
        input.close();
	}
}
