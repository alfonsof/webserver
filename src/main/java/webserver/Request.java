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
 * Manages the parsing and reading of HTTP requests from a client connection.
 *
 * @author Alfonso Fernandez-Barandiaran
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
     * Constructs a new {@code Request} instance.
     *
     * @param serverSettings settings of the web server
     * @param clientSocket   socket representing the client connection
     * @param input          input stream of the client request
     * @param reader         buffered reader for reading request lines and headers
     */
    public Request(ServerSettings serverSettings, Socket clientSocket,
                   InputStream input, BufferedReader reader) {
        this.serverSettings = serverSettings;
        this.clientSocket = clientSocket;
        this.input = input;
        this.reader = reader;
        logger.trace("Request->clientSocket: " + this.clientSocket);
        logger.trace("Request->input: " + this.input);
    }

    /**
     * Reads and parses the HTTP request line and headers from the input stream.
     *
     * @return {@code true} if the request line and headers are successfully read and valid; {@code false} otherwise
     * @throws IOException if an input or output exception occurs during reading
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
     * Returns the HTTP request line read from the client.
     *
     * @return the request line string
     */
    public String getRequestLine() {
        return this.requestLine;
    }

    /**
     * Reads the HTTP request line from the client input stream,
     * applying the configured request read timeout.
     *
     * @return the request line string, or null if the stream ends
     * @throws IOException if an input or output exception occurs
     */
    private String readRequestLine() throws IOException {
        String reqLine;
        
        clientSocket.setSoTimeout(serverSettings.getRequestReadTimeout());
        reqLine = reader.readLine();
        logger.trace("Request line: " + reqLine);
    
        return reqLine;
    }

    /**
     * Reads and parses the HTTP request headers until an empty line is encountered,
     * applying the configured header read timeout.
     *
     * @param reader the buffered reader to read header lines from
     * @return a map containing the parsed header names and lowercase values
     * @throws IOException if an input or output exception occurs
     */
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
