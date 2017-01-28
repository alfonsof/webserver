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
 * Manages the http response
 * @author      Alfonso Fernandez-Barandiaran
 * @version     1.1
 * @since       2016-12-04
 */

public class Response {
	
	private static final Pattern REQUEST_LINE_ACCEPTED = Pattern.compile("(GET|HEAD) ([^ ]+) HTTP/(\\d\\.\\d)");
	private static final String CRLF = "\r\n";
	private static final String CONTENT_TYPE_TEXT = "Content-Type";
	private static final String CONNECTION_TEXT = "Connection";
	private static final String CONTENT_LENGTH_TEXT = "Content-Length";
	private static final String TEXT_HTML_TYPE_TEXT = "text/html";
	private static Map<String,String> mapMime;
	private static final Logger logger = LogManager.getLogger(Response.class.getName());
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
	public void write() throws IOException {
   	    Matcher requestLineMatcher = REQUEST_LINE_ACCEPTED.matcher(request.getRequestLine());
	    
   	    if (requestLineMatcher.matches()) {  // Request Line accepted
   	    	String method = requestLineMatcher.group(1);
            String requestUri = requestLineMatcher.group(2);
            String httpVersion = requestLineMatcher.group(3);

            if (!httpVersionImplemented(httpVersion)) {  // http version not implemented
            	httpError(writer, httpVersion, ServerSettings.HTTP_STR_NOT_IMPLEMENTED, ServerSettings.HTTP_STR_NOT_IMPLEMENTED + "  (http version " + httpVersion + ")" );
            	logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_IMPLEMENTED + " (http version " + httpVersion + ")");
            	writer.flush();
            	return;
            }
            
            logger.trace("Request Uri: " + requestUri);
            	
           	final File f = fileAndPath(requestUri);

           	if (!pathExist(f)) {  // Path not exist
               	httpError(writer, httpVersion, ServerSettings.HTTP_STR_BAD_REQUEST, ServerSettings.HTTP_STR_BAD_REQUEST);
               	addResponseHeader(CONTENT_LENGTH_TEXT, "0");
               	logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_BAD_REQUEST);
               	writer.flush();
               	return;
            }
           			
         	if (serverSettings.getDirectoryListing() && f.isDirectory()) {  // Serve directory listing
           	    writeDirectoryListing(writer, httpVersion, f);
           	    logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_OK);
           	} else if (fileExists(f)) {  // Serve file
	    		writeReponse(output, writer, httpVersion, method, f);
	    		logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_OK);
           	} else {  // File not exist
           	    httpError(writer, httpVersion, ServerSettings.HTTP_STR_NOT_FOUND, ServerSettings.HTTP_STR_NOT_FOUND);
           	    addResponseHeader(CONTENT_LENGTH_TEXT, "0");
           	    logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_FOUND);
           	}

   	    } else {
   	    	httpError(writer, "", ServerSettings.HTTP_STR_NOT_IMPLEMENTED, ServerSettings.HTTP_STR_NOT_IMPLEMENTED);
        	logger.info(request.getRequestLine() + " -> " + ServerSettings.HTTP_STR_NOT_IMPLEMENTED);
        }
   	    writer.flush();
	}

	/**
     * Manages the http error response
     * @throws IOException If an input or output 
     *                     exception occurred
     * @param writer			Buffer for the response
     * @param httpVersion		Http version
     * @param statusCode		Status code for the response
     * @param bodyText			Text of the body in the response
     */	
	public void httpError(Writer writer, String httpVersion, String statusCode, String bodyText) throws IOException {
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
	
	private boolean httpVersionImplemented(String httpVersion) {
		if ((httpVersion != null) && ("1.0".equals(httpVersion) || "1.1".equals(httpVersion))) {
			return true;
		}
		return false;
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
        if (!filePath.exists() && !filePath.isDirectory()) {
            return false;
        }
		return true;
	}

	private boolean fileExists(File f) {
	    if (f.exists() && f.isFile() && !f.isHidden()) {
	    	return true;
	    }
	    return false;
	}
    
    private String mimeTypeByExtension(File f) {
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

    private void addResponseHeader(String header, String value) {
    	headersResponse.append(header).append(": ").append(value).append(CRLF);
    }
    
    private String getHeaders() {
        return headersResponse.toString();
    }

	private void writeStatusLineOK(Writer writer, String httpVersion) throws IOException {
		String str = "HTTP/" + httpVersion + ' ' + ServerSettings.HTTP_STR_OK + CRLF;
		writer.append(str);
    	logger.trace("Response: " + str);
	}

    private void writeBody(OutputStream os, File f) throws IOException {
        InputStream is = new FileInputStream(f);
        byte[] buffer = new byte[serverSettings.getFileBufferSize()];
        while (is.available() > 0) {
            int read = is.read(buffer);
            os.write(buffer, 0, read);
        }
        is.close();
    }
    
    private void writeReponse(OutputStream output, Writer writer, String httpVersion, String method, File f) throws IOException {
		logger.trace("Serving: " + f.getName());
		writeStatusLineOK(writer, httpVersion);
		addResponseHeader(CONTENT_TYPE_TEXT, mimeTypeByExtension(f));
		addResponseHeader(CONNECTION_TEXT, "close");
		addResponseHeader(CONTENT_LENGTH_TEXT, Long.toString(f.length()));
		writer.append(getHeaders());
		logger.trace("Response Headers: " + getHeaders());
		writer.append(CRLF);
		logger.trace("Response: CRLF");
        writer.flush();
		if (method != null && !"HEAD".equals(method)) {
			writeBody(output, f);
		}
    }

    private String listDirectory(File dir) throws IOException {
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

    private void writeDirectoryListing(Writer writer, String httpVersion, File f)  throws IOException {
		String buffer = listDirectory(f);
		int lenBuffer = buffer.length();
		logger.trace("Directory to listing: " + f.getName());
		logger.trace("Directory Listing buffer: " + buffer);
		logger.trace("Directory Listing length: " + lenBuffer);
		writeStatusLineOK(writer, httpVersion);
		addResponseHeader(CONTENT_TYPE_TEXT, TEXT_HTML_TYPE_TEXT);
		addResponseHeader(CONNECTION_TEXT, "close");
		addResponseHeader(CONTENT_LENGTH_TEXT, Integer.toString(lenBuffer));
		writer.append(getHeaders());
		logger.trace("Response Headers: " + getHeaders());
		writer.append(CRLF);
		logger.trace("Response: CRLF");
		writer.append(buffer);
		logger.trace("Response Buffer: " + buffer);
		writer.flush();
    }

}
