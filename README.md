# KeywordCounter

KeywordCounter is a Java application for counting occurrences of keywords in files and web pages. The application consists of four main components: File Scanner, Web Scanner, Result Retriever, and CLI. 

- **File Scanner:** scans all files in a given directory and counts the occurrences of keywords.
- **Web Scanner:** scrapes web pages for a given domain and counts the occurrences of keywords.
- **Result Retriever:** stores the results of the File Scanner and Web Scanner and provides methods to retrieve them.
- **CLI:** provides a command-line interface for interacting with the application.

## How to use

To use the KeywordCounter application, you need to build the project and run the `Main` class. 

### Building the project

You can build the project using Maven. In the project directory, run the following command:

```
mvn clean install
```

### Running the application

After building the project, you can run the application by executing the `Main` class. The CLI will prompt you for a command. Some of the available commands:

- `file|<directory_name>`: count occurrences of keywords in files in the specified directory.
- `web|<domain_name>`: count occurrences of keywords in web pages for the specified domain.
- `file|summary`: get a summary of occurrences of keywords in all files scanned so far.
- `web|summary`: get a summary of occurrences of keywords in all web pages scanned so far.
- `exit`: exit the application.

## Technologies used

The KeywordCounter application is built using the following technologies:

- Java 8
- Maven
- Jsoup (for web scraping)
