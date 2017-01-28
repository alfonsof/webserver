Web Server v 1.1
================

A multi-threaded (e.g. file-based) web server with thread-pooling implemented in Java.

The Web Server uses a thread-pool with a fixed numbers of threads and a thread per connection that is incoming. 

Features
--------

- Static file serving.

- Configuration file "webserver.properties". You can configure this variables:
  - ServerPort: Server port.
  - DocumentRoot: Directory where files are served.
  - DirectoryListing: Deactivate the listing of files when the URI does not content a file.
    By default is activate. To deactivate use "DirectoryListing=n"
  - ThreadsNumber: Number of threads running in the webserver thread-pool.

  Default values if the Web Server does not find a "webserver.properties" file:
  ServerPort=9090
  DocumentRoot=wwwdocs/
  DirectoryListing=y
  ThreadsNumber=10

Libraries
---------

The Web Server uses these libraries:

- Apache log4j 2 version 2.7
  It is used for logging.
  The application writes informative, warning and error messages in the console and in a rolling file in "logs" directory.
  It is configured using the "log4j2.xml" file.

- jUnit version 4.12
  It is used for testing.
  You can run the "WebServerTest" class in order to test the behaviour of the Web Server.

- Apache HttpComponents version 4.5.2
  It is used by the "WebServerTest" class in some tests in order to send command to the Web Server.

Notes
-----

- Creating the jar files in "target" with maven and goals "package" (mvn package) (Eclipse Run: Websever-package):
  - webserver.jar: 				JAR no including dependencies
  - webserver-jar-with-dependencies.jar: 	JAR with dependencies

- Creating the jar file with Eclipse in WebServer root:
    - File/Export
    - Runnable JAR file
    - Extract required libraries into generated JAR
    - webserver.jar generated

- You can run the Web Server "webserver.jar" in 2 ways:

  - Executing this command:
  
    java -jar webserver.jar

    Run the Web Server using default configuration on logging (only error messages to console).

  - Executing this command:

    java -Dlog4j.configurationFile=resources/log4j2.xml -jar webserver.jar
  
    Run the Web Server using a configuration on logging, writing informative, warning and error messages in the console and in a rolling file in "logs" directory.

- In the document root "wwwdocs", there are several documents for testing the Web Server:

  - index.html		Simple html page
  - images.html		Html page containing 49 images
  - img/*.gif		Images for images.html page
  - dir/dir.html	Directory containing a simple html page
  - imggiftest.gif	Simple image in GIF format
  - imgjpgtest.jpg	Simple image in JPG format
  - imgpngtest.png	Simple image in PNG format
  - wordtest.doc	Simple word 97-2004 document
  - wordtest.docx	Simple word document
  - pdftest.pdf		Simple pdf document
  - exceltest.xls	Simple excel 97-2004 spreadsheet
  - exceltest.xlsx	Simple excel spreadsheet

- You can test the WebServer using the class "WebServerTest".

- You can find Javadoc documentation in "doc" directory.
