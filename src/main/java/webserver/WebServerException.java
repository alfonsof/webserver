package webserver;

/**
 * Custom runtime exception used to handle errors specific to the web server execution,
 * socket management, or I/O operations.
 *
 * @author Alfonso Fernandez-Barandiaran
 */
public class WebServerException extends RuntimeException {

    /**
     * Constructs a new {@code WebServerException} with no detail message.
     */
    public WebServerException() {
        super();
    }

    /**
     * Constructs a new {@code WebServerException} with the specified detail message.
     *
     * @param s the detail message
     */
    public WebServerException(String s) {
        super(s);
    }

    /**
     * Constructs a new {@code WebServerException} with the specified detail message and cause.
     *
     * @param s         the detail message
     * @param throwable the cause of the exception
     */
    public WebServerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Constructs a new {@code WebServerException} with the specified cause.
     *
     * @param throwable the cause of the exception
     */
    public WebServerException(Throwable throwable) {
        super(throwable);
    }
}
