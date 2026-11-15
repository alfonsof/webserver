package webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages HTTP responses, handling request routing to static files or directory listings,
 * and generating appropriate HTTP success, redirect, or error responses.
 *
 * @author Alfonso Fernandez-Barandiaran
 */
public class Response {
    
    private static final Pattern REQUEST_LINE_ACCEPTED = Pattern.compile("(GET|HEAD) ([^ ]+) HTTP/(\\d\\.\\d)");
    private static final int REQUEST_LINE_ACCEPTED_MATCH_GROUP_METHOD = 1;
    private static final int REQUEST_LINE_ACCEPTED_MATCH_GROUP_REQUEST_URI = 2;
    private static final int REQUEST_LINE_ACCEPTED_MATCH_GROUP_HTTP_VERSION = 3;
    private static final String CRLF = "\r\n";
    private static final String CONTENT_TYPE_TEXT = "Content-Type";
    private static final String CONNECTION_TEXT = "Connection";
    private static final String CONTENT_LENGTH_TEXT = "Content-Length";
    private static final String TEXT_HTML_TYPE_TEXT = "text/html";
    private static final Logger logger = LogManager.getLogger(Response.class.getName());
    private static Map<String,String> mapMime;
    private ServerSettings serverSettings;
    private Request request;
    private OutputStream output;
    private final Writer writer;
    private final StringBuilder headersResponse = new StringBuilder();
    
    static {
        mapMime = new ConcurrentHashMap<>();
        mapMime.put("html", TEXT_HTML_TYPE_TEXT);
        mapMime.put("htm",  TEXT_HTML_TYPE_TEXT);
        mapMime.put("css",  "text/css");
        mapMime.put("gif",  "image/gif");
        mapMime.put("jpeg", "image/jpeg");
        mapMime.put("jpg",  "image/jpeg");
        mapMime.put("png",  "image/png");
        mapMime.put("js",   "application/javascript");
        mapMime.put("xml",  "application/xml");
        mapMime.put("pdf",  "application/pdf");
    }

    /**
     * Constructs a new {@code Response} instance.
     *
     * @param serverSettings settings of the web server
     * @param request        the client HTTP request
     * @param output         output stream for the response body
     * @param writer         buffered writer for response headers and text
     */
    public Response(ServerSettings serverSettings, Request request,
                    OutputStream output, Writer writer) {
        this.serverSettings = serverSettings;
        this.request = request; 
        this.output = output;
        this.writer = writer;
    }

    /**
     * Processes the HTTP request, determines whether to serve a file, directory listing,
     * or handle validation errors/unsupported versions, and writes the response.
     *
     * @throws IOException if an input or output exception occurs during response generation
     */
    public void writeResponse() throws IOException {
        Matcher requestLineMatcher = REQUEST_LINE_ACCEPTED.matcher(request.getRequestLine());
        
        if (requestLineMatcher.matches()) {  // Request Line accepted
            String method = requestLineMatcher.group(REQUEST_LINE_ACCEPTED_MATCH_GROUP_METHOD);
            String requestUri = requestLineMatcher.group(REQUEST_LINE_ACCEPTED_MATCH_GROUP_REQUEST_URI);
            String httpVersion = requestLineMatcher.group(REQUEST_LINE_ACCEPTED_MATCH_GROUP_HTTP_VERSION);

            if (!isHttpVersionImplemented(httpVersion)) {  // http version not implemented
                writeHttpVersionNotImplementedResponse(writer, httpVersion, request);
                return;
            }
            
            logger.trace("Request Uri: " + requestUri);
                
            final File f = fileAndPath(requestUri);

            if (!pathExist(f)) {  // Path not exist
                writePathNotExistResponse(writer, httpVersion, request);
                return;
            }
                    
            if (serverSettings.getDirectoryListing() && f.isDirectory()) {  // Serve directory listing
                writeDirectoryListingResponse(writer, httpVersion, f);
            } else if (fileExists(f)) {  // Serve file
                writeFileOKReponse(writer, httpVersion, method, output, f);
            } else {  // File not exist
                writeFileKOReponse(writer, httpVersion, request);
            }

        } else {     // Request Line not accepted
            writeNotImplementedResponse(writer, request);
        }
    }

    /**
     * Writes an HTTP Bad Request (400) error response to the client.
     *
     * @param writer buffer for the response
     * @throws IOException if an input or output exception occurs
     */
    public void writeBadRequestResponse(Writer writer) throws IOException {
        writeHttpError(writer, "", ServerSettings.HTTP_STR_BAD_REQUEST, ServerSettings.HTTP_STR_BAD_REQUEST);
        writer.flush();
        logger.info(ServerSettings.HTTP_STR_BAD_REQUEST);
    }

    /**
     * Writes an HTTP Request Timeout (408) error response to the client.
     *
     * @param writer buffer for the response
     * @throws IOException if an input or output exception occurs
     */
    public void writeRequestTimeoutResponse(Writer writer) throws IOException {
        writeHttpError(writer, "", ServerSettings.HTTP_STR_REQUEST_TIMEOUT, ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
        writer.flush();
        logger.info(ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
    }

    /**
     * Writes an HTTP Internal Server Error (500) response to the client.
     *
     * @param writer buffer for the response
     * @throws IOException if an input or output exception occurs
     */
    public void writeServerErrortResponse(Writer writer) throws IOException {
        writeHttpError(writer, "", ServerSettings.HTTP_STR_SERVER_ERROR, ServerSettings.HTTP_STR_SERVER_ERROR);
        writer.flush();
        logger.info(ServerSettings.HTTP_STR_SERVER_ERROR);
    }

    /**
     * Writes a response indicating that the requested HTTP version is not supported or implemented.
     *
     * @param writer      buffer for the response
     * @param httpVersion the unsupported HTTP version string
     * @param request     the client request object
     * @throws IOException if an input or output exception occurs
     */
    private void writeHttpVersionNotImplementedResponse(Writer writer, String httpVersion, Request request)  throws IOException { 
        writeHttpError(writer, httpVersion, ServerSettings.HTTP_STR_NOT_IMPLEMENTED, ServerSettings.HTTP_STR_NOT_IMPLEMENTED + "  (http version " + httpVersion + ")" );
        writer.flush();
        logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_IMPLEMENTED + " (http version " + httpVersion + ")");
    }

    /**
     * Writes a response indicating that the requested path does not exist.
     *
     * @param writer      buffer for the response
     * @param httpVersion the HTTP version used in the request
     * @param request     the client request object
     * @throws IOException if an input or output exception occurs
     */
    private void writePathNotExistResponse(Writer writer, String httpVersion, Request request)  throws IOException {
        writeHttpError(writer, httpVersion, ServerSettings.HTTP_STR_BAD_REQUEST, ServerSettings.HTTP_STR_BAD_REQUEST);
        addResponseHeader(CONTENT_LENGTH_TEXT, "0");
        writer.flush();
        logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_BAD_REQUEST);
    }

    /**
     * Generates and writes an HTML directory listing response for the specified directory.
     *
     * @param writer      buffer for the response headers and body
     * @param httpVersion the HTTP version used in the request
     * @param f           the directory file object
     * @throws IOException if an input or output exception occurs
     */
    private void writeDirectoryListingResponse(Writer writer, String httpVersion, File f)  throws IOException {
        String buffer = buildDirectoryList(f);
        int lenBuffer = buffer.length();
        logger.trace("Directory to listing: " + f.getName());
        logger.trace("Directory Listing buffer: " + buffer);
        logger.trace("Directory Listing length: " + lenBuffer);
        writeStatusLineOK(writer, httpVersion);
        addResponseHeader(CONTENT_TYPE_TEXT, TEXT_HTML_TYPE_TEXT);
        addResponseHeader(CONNECTION_TEXT, "close");
        addResponseHeader(CONTENT_LENGTH_TEXT, Integer.toString(lenBuffer));
        writer.append(getResponseHeaders());
        logger.trace("Response Headers: " + getResponseHeaders());
        writer.append(CRLF);
        logger.trace("Response: CRLF");
        writer.append(buffer);
        logger.trace("Response Buffer: " + buffer);
        writer.flush();
        logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_OK);
    }

    /**
     * Writes a successful HTTP 200 OK response containing the requested file content.
     *
     * @param writer      buffer for the response headers
     * @param httpVersion the HTTP version used in the request
     * @param method      the HTTP request method (e.g., GET, HEAD)
     * @param output      output stream to write the file binary body
     * @param f           the file to be served
     * @throws IOException if an input or output exception occurs
     */
    private void writeFileOKReponse(Writer writer, String httpVersion, String method, OutputStream output, File f) throws IOException {
        logger.trace("Serving: " + f.getName());
        writeStatusLineOK(writer, httpVersion);
        addResponseHeader(CONTENT_TYPE_TEXT, getMimeTypeByExtension(f));
        addResponseHeader(CONNECTION_TEXT, "close");
        addResponseHeader(CONTENT_LENGTH_TEXT, Long.toString(f.length()));
        writer.append(getResponseHeaders());
        logger.trace("Response Headers: " + getResponseHeaders());
        writer.append(CRLF);
        logger.trace("Response: CRLF");
        writer.flush();
        if (method != null && !"HEAD".equals(method)) {
            writeBody(output, f);
        }
        writer.flush();
        logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_OK);
    }

    /**
     * Writes an HTTP 404 Not Found error response when the requested file does not exist.
     *
     * @param writer      buffer for the response
     * @param httpVersion the HTTP version used in the request
     * @param request     the client request object
     * @throws IOException if an input or output exception occurs
     */
    private void writeFileKOReponse(Writer writer, String httpVersion, Request request) throws IOException {
        writeHttpError(writer, httpVersion, ServerSettings.HTTP_STR_NOT_FOUND, ServerSettings.HTTP_STR_NOT_FOUND);
        addResponseHeader(CONTENT_LENGTH_TEXT, "0");
        writer.flush();
        logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_FOUND);
    }

    /**
     * Writes an HTTP 501 Not Implemented response for unsupported HTTP methods.
     *
     * @param writer  buffer for the response
     * @param request the client request object
     * @throws IOException if an input or output exception occurs
     */
    private void writeNotImplementedResponse(Writer writer, Request request) throws IOException {
        writeHttpError(writer, "", ServerSettings.HTTP_STR_NOT_IMPLEMENTED, ServerSettings.HTTP_STR_NOT_IMPLEMENTED);
        writer.flush();
        logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_IMPLEMENTED);
    }

    /**
     * Appends an HTTP header key-value pair to the response headers buffer.
     *
     * @param header the header name
     * @param value  the header value
     */
    private void addResponseHeader(String header, String value) {
        headersResponse.append(header).append(": ").append(value).append(CRLF);
    }

    /**
     * Writes the binary file body content to the output stream.
     *
     * @param os the output stream to write data into
     * @param f  the file whose content is being written
     * @throws IOException if an input or output exception occurs
     */
    private void writeBody(OutputStream os, File f) throws IOException {
        try (InputStream is = new FileInputStream(f);
        ) {
            byte[] buffer = new byte[serverSettings.getFileBufferSize()];
            while (is.available() > 0) {
                int read = is.read(buffer);
                os.write(buffer, 0, read);
            }
        }
    }

    /**
     * Writes the HTTP status line for a successful response (HTTP/x.x 200 OK).
     *
     * @param writer      buffer for the response
     * @param httpVersion the HTTP version string
     * @throws IOException if an input or output exception occurs
     */
    private void writeStatusLineOK(Writer writer, String httpVersion) throws IOException {
        String str = "HTTP/" + httpVersion + ' ' + ServerSettings.HTTP_STR_OK + CRLF;
        writer.append(str);
        logger.trace("Response: " + str);
    }

    /**
     * Formats and writes an HTTP error response including status code and HTML body text.
     *
     * @param writer      buffer for the response
     * @param httpVersion the HTTP version string
     * @param statusCode  the HTTP status code description
     * @param bodyText    the text message to display inside the HTML body
     * @throws IOException if an input or output exception occurs
     */
    private void writeHttpError(Writer writer, String httpVersion, String statusCode, String bodyText) throws IOException {
        String httpVersionFilled;
        
        if  (httpVersion == null || "".equals(httpVersion)) {
            httpVersionFilled = "1.1";
        } else {
            httpVersionFilled = httpVersion;
        }
        writer.append("HTTP/").append(httpVersionFilled).append(" ").append(statusCode).append(CRLF+CRLF);
        
        if  (bodyText != null && !"".equals(bodyText)) {
            writer.append("<html><title></title><body>");
            writer.append("<h1>").append(bodyText).append("</h1>");
            writer.append("</body></html>");
        }
    }

    /**
     * Checks if the specified HTTP version is supported by the server (HTTP/1.0 or HTTP/1.1).
     *
     * @param httpVersion the HTTP version string to check
     * @return {@code true} if implemented; {@code false} otherwise
     */
    private boolean isHttpVersionImplemented(String httpVersion) {
        return (httpVersion != null) && ("1.0".equals(httpVersion) || "1.1".equals(httpVersion));
    }

    /**
     * Resolves the requested URI string into a safe file path within the server's document root.
     *
     * @param path the requested URI path
     * @return the resolved {@link File} object
     */
    private File fileAndPath(String path) {
        String modifiedPath = path.replace("..", "");  // Remove possible parent path ".."
        while (modifiedPath.startsWith("/")) {  // Strip off leading slashes
            modifiedPath = modifiedPath.substring(1);
        }
        return new File(serverSettings.getDocumentRoot(), modifiedPath);
    }

    /**
     * Retrieves the parent directory absolute path of a given file.
     *
     * @param file the file object
     * @return the parent directory path string
     */
    private String getStrPath(File file) {
        String absolutePath = file.getAbsolutePath();
        
        return absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
    }

    /**
     * Verifies whether the parent path of the given file exists.
     *
     * @param file the file object to check
     * @return {@code true} if the path exists; {@code false} otherwise
     */
    private boolean pathExist(File file) {
        String strPath = getStrPath(file);
        File filePath = new File(strPath);
        return !(!filePath.exists() && !filePath.isDirectory());
    }

    /**
     * Checks if a file exists, is a standard file (not a directory), and is not hidden.
     *
     * @param f the file to check
     * @return {@code true} if it's a valid readable file; {@code false} otherwise
     */
    private boolean fileExists(File f) {
        return f.exists() && f.isFile() && !f.isHidden();
    }

    /**
     * Determines the MIME type of a file based on its file extension.
     *
     * @param f the file object
     * @return the corresponding MIME type string, or an empty string if unknown
     */
    private String getMimeTypeByExtension(File f) {
        String fileName = f.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        
        String mimeType = mapMime.get(extension);
        logger.trace("Extension file: " + extension);
        logger.trace("Mime type: " + mimeType);
        if (mimeType != null) {
            return mimeType;
        } else {  // unknown mime type of file
            // Omit the type in order to allow the recipient to guess the type instead of using Arbitrary binary data: "application/octet-stream"
            return "";
        }
    }

    /**
     * Returns the accumulated response headers as a single string.
     *
     * @return the response headers string
     */
    private String getResponseHeaders() {
        return headersResponse.toString();
    }

    /**
     * Builds an HTML string representing the directory listing for the given folder.
     *
     * @param dir the directory file object
     * @return the HTML formatted directory listing string
     */
    private String buildDirectoryList(File dir) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>\n<title>Directory listing</title>\n<body>\n"). 
               append("<a href=\"..\">Parent Directory</a><br>\n");
        String[] list = dir.list();
        for (int i = 0; list != null && i < list.length; i++) {
            File f = new File(dir, list[i]);
            if (f.isDirectory()) {
                buffer.append("<a href=\""+list[i]+"/\">"+list[i]+"/</a><br>");
            } else {
                buffer.append("<a href=\""+list[i]+"\">"+list[i]+"</a><br>");
            }
        }
        buffer.append("<p><hr><br><i>" + (new Date()) + "</i>\n");
        buffer.append("</body>\n</html>\n");
        
        return buffer.toString();
    }
}
