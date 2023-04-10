package main.application;

import main.crawler.DirectoryCrawler;
import main.enums.JobStatus;
import main.enums.ScanType;
import main.job.Job;
import main.dispatcher.JobDispatcher;
import main.result.ResultRetrieverImpl;
import main.scanners.file_scanner.FileScanThreadPool;
import main.scanners.web_scanner.WebScanThreadPool;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        var jobQueue = new LinkedBlockingQueue<Job>();
        BlockingQueue<Future<Map<String, Integer>>> resultQueue = new LinkedBlockingQueue<>();

        Properties props = new Properties();
        try {
            InputStream input = new FileInputStream("src/resources/app.properties");
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        List<String> keywords =  Arrays.asList(props.getProperty("keywords").split(","));
        String file_corpus_prefix = props.getProperty("file_corpus_prefix");
        int dir_crawler_sleep_time = Integer.parseInt(props.getProperty("dir_crawler_sleep_time"));
        int file_scanning_size_limit = Integer.parseInt(props.getProperty("file_scanning_size_limit"));
        int hop_count = Integer.parseInt(props.getProperty("hop_count"));
        long url_refresh_time = Long.parseLong(props.getProperty("url_refresh_time"));

        // Component initialization
        var directoryCrawler = new DirectoryCrawler(file_corpus_prefix, dir_crawler_sleep_time, jobQueue);
        var directoryCrawlerThread = new Thread(directoryCrawler);
        directoryCrawlerThread.start();

        var resultRetriever = new ResultRetrieverImpl(resultQueue);
        var resultRetrieverThread = new Thread(resultRetriever);
        resultRetrieverThread.start();

        var fileScanThreadPool = new FileScanThreadPool(file_scanning_size_limit, keywords, resultQueue);
        var webScanThreadPool = new WebScanThreadPool(hop_count, url_refresh_time, keywords);

        var jobDispatcher = new JobDispatcher(jobQueue, fileScanThreadPool, webScanThreadPool);
        var jobDispatcherThread = new Thread(jobDispatcher);
        jobDispatcherThread.start();

        Scanner scanner = new Scanner(System.in);
        boolean shutdown = false;

        while (!shutdown) {
            String userInput = scanner.nextLine();
            String[] command = userInput.split(" ");

            if (command.length > 2) {
                System.out.println("Too many arguments, commands have maximum two parameters!");
            }
            else switch (command[0]) {
                case "ad":
                    String directoryPath = command[1];
                    File directory = new File(directoryPath);
                    if (!directory.exists()) {
                        System.out.println("The directory does not exists!");
                    } else if (!directory.isDirectory()) {
                        System.out.println("The file is not a directory. You must specify a path to directory!");
                    }

                    directoryCrawler.setDirectory(directory);

                    break;
                case "aw":
                    try {
                        jobQueue.put(new Job(ScanType.WEB, command[1], JobStatus.RUNNING));
                    } catch (InterruptedException e) {

                    }
                    break;
                case "get":
                    String[] splitCommand = command[1].split("|");
                    Map<String, Integer> result = resultRetriever.getResult(splitCommand[1]);
                    for (Map.Entry<String, Integer> entry : result.entrySet()) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    }
                    break;
                case "query":

                    break;
                case "cfs":
                    break;
                case "cws":
                    break;
                case "stop":
                    System.out.println("Stopping...");
                    shutdown = true;
                    directoryCrawler.stopThread();
                    jobDispatcher.stopThread();

                    try {
                        directoryCrawlerThread.join();
                        jobDispatcherThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    System.out.println("Command does not exists!");
            }
        }
    }
}
//ad src/testcases/data
//ad src/testcases/data2
// aw