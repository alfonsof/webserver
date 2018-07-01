/**
 * Test the Web Server
*/

package webserver;

import static org.junit.Assert.assertEquals;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Web Server
 * @author      Alfonso Fernandez-Barandiaran
 */
public class WebServerTest {

    private static final String LOCAL_HOST_TEXT = "http://localhost:";
    private static final String USER_AGENT_TEXT = "User-Agent";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String SENDING_GET_MESSAGE = "\nSending 'GET' request to URL : ";
    private static final String RESPONSE_CODE_MESSAGE = "Response Code : ";
    private static final Logger logger = LogManager.getLogger(WebServerTest.class.getName());
    private static StringBuilder testUrl = new StringBuilder(LOCAL_HOST_TEXT);
    private static StringBuilder testBadRequestUrl = new StringBuilder(LOCAL_HOST_TEXT);
    private static StringBuilder testNotFoundUrl = new StringBuilder(LOCAL_HOST_TEXT);

    /**
     * Setup before tests
     */
    @BeforeClass 
    public static void setUpBeforeClass() {
        ServerSettings serverSettings = new ServerSettings(true);
        int serverPort = serverSettings.getServerPort();
        logger.info("\nServerPort : " + serverPort);
        testUrl.append(Integer.toString(serverPort)).append("/index.html");
        logger.info( testUrl );
        testBadRequestUrl.append(Integer.toString(serverPort)).append("/nothing/index.html");
        logger.info( testBadRequestUrl );
        testNotFoundUrl.append(Integer.toString(serverPort)).append("/dir/nothing.html");
        logger.info( testNotFoundUrl );
    }
    
    /**
     * Test http GET request
     */
    @Test
    public void testGETRequest() {
        try {
            String url = testUrl.toString();
            URL urlobj = new URL(url);  
        
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty(USER_AGENT_TEXT, USER_AGENT);
            logger.info(SENDING_GET_MESSAGE + url);
            int responseCode = con.getResponseCode();
            logger.info(RESPONSE_CODE_MESSAGE + responseCode);
        
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
        } catch (IOException e) {
            logger.error("testGETRequest: ", e);
        }
    }

    /**
     * Test http HEAD request
     */
    @Test
    public void testHEADRequest() {
        try {
            String url = testUrl.toString();
            URL urlobj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();

            con.setRequestMethod("HEAD");
            con.setRequestProperty(USER_AGENT_TEXT, USER_AGENT);
            logger.info("\nSending 'HEAD' request to URL : " + url);
            int responseCode = con.getResponseCode();
            logger.info(RESPONSE_CODE_MESSAGE + responseCode);
        
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
        } catch (IOException e) {
            logger.error("testHEADRequest: ", e);
        }
    }

    /**
     * Test http POST request
     */
    @Test
    public void testPOSTRequest() {
        notImplementedCommand("POST");
    }

    /**
     * Test http OPTIONS request
     */
    @Test
    public void testOPTIONSRequest() {
        notImplementedCommand("OPTIONS");
    }

    /**
     * Test http PUT request
     */
    @Test
    public void testPUTRequest() {
        notImplementedCommand("PUT");
    }

    /**
     * Test http DELETE request
     */
    @Test
    public void testDELETERequest() {
        notImplementedCommand("DELETE");
    }

    /**
     * Test http TRACE request
     */
    @Test
    public void testTRACERequest() {
        notImplementedCommand("TRACE");
    }

    /**
     * Test http GET request for a right URL
     */
    @Test
    public void testOKURL() {
        try {
            String url = testUrl.toString();
            URL urlobj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty(USER_AGENT_TEXT, USER_AGENT);
            logger.info(SENDING_GET_MESSAGE + url);
            int responseCode = con.getResponseCode();
            logger.info(RESPONSE_CODE_MESSAGE + responseCode);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
 
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
 
            logger.info(response.toString());
        
            assertEquals(HttpURLConnection.HTTP_OK, responseCode);
        } catch (IOException e) {
            logger.error("testOKURL: ", e);
        }
    }

    /**
     * Test http GET request for a bad URL
     */
    @Test
    public void testBadRequestURL() {
        try {
            String url = testBadRequestUrl.toString();
            URL urlobj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty(USER_AGENT_TEXT, USER_AGENT);
            logger.info(SENDING_GET_MESSAGE + url);
            int responseCode = con.getResponseCode();
            logger.info(RESPONSE_CODE_MESSAGE + responseCode);
        
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode);
        } catch (IOException e) {
            logger.error("testBadRequestURL: ", e);
        }
    }

    /**
     * Test http GET request for a not found URL
     */
    @Test
    public void testNotFoundURL() {
        try {
            String url = testNotFoundUrl.toString();
            URL urlobj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty(USER_AGENT_TEXT, USER_AGENT);
            logger.info(SENDING_GET_MESSAGE + url);
            int responseCode = con.getResponseCode();
            logger.info(RESPONSE_CODE_MESSAGE + responseCode);
        
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode);
        } catch (IOException e) {
            logger.error("testNotFoundURL: ", e);
        }
    }

    /**
     * Test http GET request version 1.1
     */
    @Test
    public void testImplementedHttp11Version() {
        try {
            String url = testUrl.toString();
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setProtocolVersion(HttpVersion.HTTP_1_1);
            httpGet.addHeader(USER_AGENT_TEXT, USER_AGENT);
            logger.info("\nSending 'GET' 1.1 request to URL : " + url);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            logger.info(response.getStatusLine());
    
            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            logger.error("testImplementedHttp11Version: ", e);
        }
    }

    /**
     * Test http GET request version 1.0
     */
    @Test
    public void testImplemented10Version() {
        try {
            String url = testUrl.toString();
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setProtocolVersion(HttpVersion.HTTP_1_0);
            httpGet.addHeader(USER_AGENT_TEXT, USER_AGENT);
            logger.info("\nSending 'GET' 1.0 request to URL : " + url);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            logger.info(response.getStatusLine());
    
            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            logger.error("testImplemented10Version: ", e);
        }
    }

    /**
     * Test http GET request version 0.9
     */
    @Test
    public void testNotImplementedHttp09Version() {
        try {
            String url = testUrl.toString();
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setProtocolVersion(HttpVersion.HTTP_0_9);
            httpGet.addHeader(USER_AGENT_TEXT, USER_AGENT);
            logger.info("\nSending 'GET' 0.9 request to URL : " + url);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            logger.info(response.getStatusLine());
    
            assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            logger.error("testNotImplementedHttp09Version: ", e);
        }
    }
    
    private void notImplementedCommand(String command) {
        try {
            String url = testUrl.toString();
            URL urlobj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();

            con.setRequestMethod(command);
            con.setRequestProperty(USER_AGENT_TEXT, USER_AGENT);
            logger.info("\nSending '"+ command + "' request to URL : " + url);
            int responseCode = con.getResponseCode();
            logger.info(RESPONSE_CODE_MESSAGE + responseCode);
        
            assertEquals(HttpURLConnection.HTTP_NOT_IMPLEMENTED, responseCode);
        } catch (IOException e) {
            logger.error("test" + command +"Request: ", e);
        }
    }
}
