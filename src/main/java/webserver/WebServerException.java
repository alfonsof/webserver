/**
 * WebServerException: Manages the WebServerException
 */

package webserver;

/**
 * Manages the WebServerException
 * @author      Alfonso Fernandez-Barandiaran
 */
public class WebServerException extends RuntimeException {
    
    /**
     * Class constructor
     */
    public WebServerException() {
        super();
    }

    /**
     * Class constructor
     * @param s     Message
     */
    public WebServerException(String s) {
        super(s);
    }
    
    /**
     * Class constructor
     * @param s             Message
     * @param throwable     Exception
     */
    public WebServerException(String s, Throwable throwable) {
        super(s, throwable);
    }
    
    /**
     * Class constructor
     * @param throwable     Exception
     */
    public WebServerException(Throwable throwable) {
        super(throwable);
    }
}
