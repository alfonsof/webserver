/**
 * Response: Manages the http response
 * @author      Alfonso Fernandez-Barandiaran
 */

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
     * Class constructor
     * @param serverSettings	Settings of the Web Server
     * @param request			Request
     * @param output			Output of the response
     * @param writer			Buffer for the response
     */
	public Response(ServerSettings serverSettings, Request request, OutputStream output, Writer writer) {
		this.serverSettings = serverSettings;
	    this.request = request; 
	    this.output = output;
	    this.writer = writer;
	}

	/**
     * Manages the http response
     * @throws IOException If an input or output 
     *                     exception occurred
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
     * Manages the http Bad Request error response
     * @throws IOException If an input or output 
     *                     exception occurred
     * @param writer			Buffer for the response
     */	
    public void writeBadRequestResponse(Writer writer) throws IOException {
    	writeHttpError(writer, "", ServerSettings.HTTP_STR_BAD_REQUEST, ServerSettings.HTTP_STR_BAD_REQUEST);
    	writer.flush();
    	logger.info(ServerSettings.HTTP_STR_BAD_REQUEST);
    }

	/**
     * Manages the http Request Timeout error response
     * @throws IOException If an input or output 
     *                     exception occurred
     * @param writer			Buffer for the response
     */	
    public void writeRequestTimeoutResponse(Writer writer) throws IOException {
	   	writeHttpError(writer, "", ServerSettings.HTTP_STR_REQUEST_TIMEOUT, ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
	   	writer.flush();
	   	logger.info(ServerSettings.HTTP_STR_REQUEST_TIMEOUT);
    }

	/**
     * Manages the http Server error response
     * @throws IOException If an input or output 
     *                     exception occurred
     * @param writer			Buffer for the response
     */	
    public void writeServerErrortResponse(Writer writer) throws IOException {
	   	writeHttpError(writer, "", ServerSettings.HTTP_STR_SERVER_ERROR, ServerSettings.HTTP_STR_SERVER_ERROR);
	   	writer.flush();
	   	logger.info(ServerSettings.HTTP_STR_SERVER_ERROR);
    }
    
    private void writeHttpVersionNotImplementedResponse(Writer writer, String httpVersion, Request request)  throws IOException { 
		writeHttpError(writer, httpVersion, ServerSettings.HTTP_STR_NOT_IMPLEMENTED, ServerSettings.HTTP_STR_NOT_IMPLEMENTED + "  (http version " + httpVersion + ")" );
		writer.flush();
		logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_IMPLEMENTED + " (http version " + httpVersion + ")");
	}

	private void writePathNotExistResponse(Writer writer, String httpVersion, Request request)  throws IOException {
		writeHttpError(writer, httpVersion, ServerSettings.HTTP_STR_BAD_REQUEST, ServerSettings.HTTP_STR_BAD_REQUEST);
       	addResponseHeader(CONTENT_LENGTH_TEXT, "0");
       	writer.flush();
       	logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_BAD_REQUEST);
	}
	
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

    private void writeFileKOReponse(Writer writer, String httpVersion, Request request) throws IOException {
    	writeHttpError(writer, httpVersion, ServerSettings.HTTP_STR_NOT_FOUND, ServerSettings.HTTP_STR_NOT_FOUND);
	    addResponseHeader(CONTENT_LENGTH_TEXT, "0");
	    writer.flush();
	    logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_FOUND);
    }
    
    private void writeNotImplementedResponse(Writer writer, Request request) throws IOException {
    	writeHttpError(writer, "", ServerSettings.HTTP_STR_NOT_IMPLEMENTED, ServerSettings.HTTP_STR_NOT_IMPLEMENTED);
		writer.flush();
		logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_IMPLEMENTED);
    }

    private void addResponseHeader(String header, String value) {
    	headersResponse.append(header).append(": ").append(value).append(CRLF);
    }
    
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
    
    private void writeStatusLineOK(Writer writer, String httpVersion) throws IOException {
		String str = "HTTP/" + httpVersion + ' ' + ServerSettings.HTTP_STR_OK + CRLF;
		writer.append(str);
    	logger.trace("Response: " + str);
	}

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
   	
	private boolean isHttpVersionImplemented(String httpVersion) {
		return (httpVersion != null) && ("1.0".equals(httpVersion) || "1.1".equals(httpVersion));
	}

	private File fileAndPath(String path) {
		String modifiedPath = path.replace("..", "");  // Remove possible parent path ".."
        while (modifiedPath.startsWith("/")) {  // Strip off leading slashes
        	modifiedPath = modifiedPath.substring(1);
        }
        return new File(serverSettings.getDocumentRoot(), modifiedPath);
    }
	
	private String getStrPath(File file) {
		String absolutePath = file.getAbsolutePath();
		
		return absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
	}
	
	private boolean pathExist(File file) {
		String strPath = getStrPath(file);
		File filePath = new File(strPath);
        return !(!filePath.exists() && !filePath.isDirectory());
	}

	private boolean fileExists(File f) {
	    return f.exists() && f.isFile() && !f.isHidden();
	}
    
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

    private String getResponseHeaders() {
        return headersResponse.toString();
    }

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
