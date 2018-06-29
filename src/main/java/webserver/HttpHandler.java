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
   	    	if (request.readRequest()) {
   	   	    	logger.trace("handleConnection - writeResponse");
   	   	    	response.writeResponse();
   	    	} else { // Wrong message in read()
   	    		logger.trace("handleConnection - BadRequest");
   	    		response.writeBadRequestResponse(writer);
   	    	}
   	    } catch (SocketTimeoutException e) {
   	    	logger.trace("handleConnection - SocketTimeoutException: ", e);
   	    	response.writeRequestTimeoutResponse(writer);
   	    } catch (SocketException e) {
   	    	logger.trace("handleConnection - SocketException: ", e);
   	    	response.writeServerErrortResponse(writer);
   	    }

        writer.close();
        input.close();
	}
}
