# Java Multithreaded Webserver
A lightweight, multithreaded HTTP web server built from scratch in Java, featuring a custom fixed thread-pool architecture, robust concurrency handling, and static file/directory serving capabilities.

## Key Features

* **Custom Thread-Pooling Architecture:** Utilizes an `ExecutorService` fixed thread pool (`ThreadPooledWebServer` & `HttpWorker`) to manage concurrent client connections efficiently.
* **Socket & I/O Management:** Handles TCP client sockets with configurable read timeouts (`SocketTimeoutException` protection) for request lines and headers.
* **Static File & Directory Serving:** Automatically resolves request URIs against a configured document root (`wwwdocs/`), supporting MIME-type mapping (HTML, CSS, JS, images, PDF) and optional HTML directory listings.
* **Robust Error Handling & Logging:** Generates standard HTTP status responses (`200 OK`, `400 Bad Request`, `404 Not Found`, `408 Request Timeout`, `500 Server Error`, `501 Not Implemented`) integrated with **Log4j2**.
* **Externalized Configuration:** Easily configurable via `webserver.properties` loaded from the classpath.

## Configuration

* The server is configured via the `webserver.properties` file. You can customize the following properties:
  * `ServerPort`: Server port.
  * `DocumentRoot`: Directory where files are served from.
  * `DirectoryListing`: Disables directory listing when the URI does not point to a specific file (`y` by default; set to `n` to disable).
  * `ThreadsNumber`: Number of threads running in the webserver thread-pool.

* If no `webserver.properties` file is found, the server uses these default values:
  * `ServerPort=9090`
  * `DocumentRoot=wwwdocs/`
  * `DirectoryListing=y`
  * `ThreadsNumber=10`

## Requirements

* The code was written for:
 
  *  Java 8

## Libraries

The Web Server uses these libraries:

## Libraries

The Web Server uses the following libraries:

- **Apache Log4j 2:** Used for logging. Writes informative, warning, and error messages to the console and to a rolling file in the `logs/` directory (configured via `log4j2.xml`).
- **JUnit 4:** Used for unit and integration testing via the `WebServerTest` class.
- **Apache HttpComponents:** Used by `WebServerTest` to send HTTP commands to the Web Server during tests.


## Build & Run

## Build & Run

### 1. Build the JAR file
Create `webserver.jar` with all dependencies:
* **Maven:** `mvn package`
* **Eclipse:** Export via **File > Export > Runnable JAR file**, selecting **Extract required libraries into generated JAR** as `webserver.jar`.

### 2. Run the Web Server
* **Default logging** (errors only to console):
  ```bash
  java -jar webserver.jar
  ```
* **Custom Log4j2 configuration** (detailed logs to console and `logs/` directory):
  ```bash
  java -Dlog4j.configurationFile=resources/log4j2.xml -jar webserver.jar
  ```
Access the server at `http://localhost:9090` or `http://localhost:9090/index.html`.

### 3. Sample Files in `wwwdocs/`
The document root includes test files such as `index.html`, `images.html` (with associated `.gif` images), subdirectories (`dir/dir.html`), and various sample formats (`.gif`, `.jpg`, `.png`, `.doc`, `.docx`, `.pdf`, `.xls`, `.xlsx`).

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

* The Javadoc documentation is available in the `docs` directory.

## License

This code is released under the MIT License. See LICENSE file.
