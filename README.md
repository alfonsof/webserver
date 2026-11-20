# Java Multithreaded Webserver
A lightweight, multithreaded HTTP web server built from scratch in Java, featuring a custom fixed thread-pool architecture, robust concurrency handling, and static file/directory serving capabilities.

## Key Features

* **Custom Thread-Pooling Architecture:** Utilizes an `ExecutorService` fixed thread pool (`ThreadPooledWebServer` & `HttpWorker`) to manage concurrent client connections efficiently.
* **Socket & I/O Management:** Handles TCP client sockets with configurable read timeouts (`SocketTimeoutException` protection) for request lines and headers.
* **Static File & Directory Serving:** Automatically resolves request URIs against a configured document root (`wwwdocs/`), supporting MIME-type mapping (HTML, CSS, JS, images, PDF) and optional HTML directory listings.
* **Robust Error Handling & Logging:** Generates standard HTTP status responses (`200 OK`, `400 Bad Request`, `404 Not Found`, `408 Request Timeout`, `500 Server Error`, `501 Not Implemented`) integrated with **Log4j2**.
* **Externalized Configuration:** Easily configurable via `webserver.properties` loaded from the classpath.

## Configuration

* Configuration file `webserver.properties`. You can configure these variables:
  * `ServerPort`: Server port.
  * `DocumentRoot`: Directory where files are served.
  * `DirectoryListing`: Deactivates the listing of files when the URI does not contain a file.
    By default, it is activated. To deactivate use `DirectoryListing=n`
  * `ThreadsNumber`: Number of threads running in the webserver thread-pool.

  Default values if the Web Server does not find a "webserver.properties" file:
  * `ServerPort=9090`
  * `DocumentRoot=wwwdocs/`
  * `DirectoryListing=y`
  * `ThreadsNumber=10`

## Requirements

* The code was written for:
 
  *  Java 8

## Libraries

The Web Server uses these libraries:

* `Apache log4j 2`
  It is used for logging.
  The application writes informative, warning and error messages in the console and in a rolling file in "logs" directory.
  It is configured using the "log4j2.xml" file.

* `JUnit 4`
  It is used for testing.
  You can run the "WebServerTest" class in order to test the behavior of the Web Server.

* `Apache HttpComponents`
  It is used by the "WebServerTest" class in some tests in order to send command to the Web Server.

## Build & Run

* Create the jar file:

  You can create it in 2 ways:
  
  * Using Maven and goals "package" (mvn package) (Eclipse Run: Webserver-package):
    * `webserver.jar`: 				JAR including dependencies

  * Using Eclipse in WebServer project:
    * File/Export
    * Runnable JAR file
    * Extract required libraries into generated JAR
    * webserver.jar generated

* Run the Web Server `webserver.jar`:
  
  You can run it in 2 ways:

  * Using default configuration on logging (only error messages to console).
  
    Executing this command:
  
    ```
    java -jar webserver.jar
    ```
    
  * Accessing the web server from a browser:
    ```
    http://localhost:9090/index.html
    ```

    ```
    http://localhost:9090
    ```

  * Using a configuration on logging, writing informative, warning and error messages in the console and in a rolling file in `logs` directory.
  
    Executing this command:

    ```
    java -Dlog4j.configurationFile=resources/log4j2.xml -jar webserver.jar
    ```
    
* In the document root `wwwdocs`, there are several documents for testing the Web Server:

  | File		    | Comment                                   |
  | --------------- | ----------------------------------------- |
  | index.html		| Simple html page                          |
  | images.html		| Html page containing 49 images            |
  | img/*.gif		| Images for images.html page               |
  | dir/dir.html	| Directory containing a simple html page   |
  | imggiftest.gif	| Simple image in GIF format                |
  | imgjpgtest.jpg	| Simple image in JPG format                |
  | imgpngtest.png	| Simple image in PNG format                |
  | wordtest.doc	| Simple word 97-2004 document              |
  | wordtest.docx	| Simple word document                      |
  | pdftest.pdf		| Simple pdf document                       |
  | exceltest.xls	| Simple excel 97-2004 spreadsheet          |
  | exceltest.xlsx	| Simple excel spreadsheet                  |

## Testing

* You can test the WebServer using the class `WebServerTest`.

## Documentation

* The Javadoc documentation in `docs` directory.

## License

This code is released under the MIT License. See LICENSE file.
