package main.application;

import main.crawler.DirectoryCrawler;
import main.enums.JobStatus;
import main.enums.ScanType;
import main.dispatcher.JobDispatcher;
import main.job.ScanningJob;
import main.job.WebJob;
import main.result.file_result.FileRetriever;
import main.result.web_result.WebRetriever;
import main.scanners.file_scanner.FileScanThreadPool;
import main.scanners.web_scanner.WebScanThreadPool;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        var jobQueue = new LinkedBlockingQueue<ScanningJob>();

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

        var fileRetriever = new FileRetriever();
        var webRetriever = new WebRetriever(url_refresh_time);

        var fileScanThreadPool = new FileScanThreadPool(file_scanning_size_limit, keywords, fileRetriever);
        var webScanThreadPool = new WebScanThreadPool(jobQueue, webRetriever, keywords);

        webRetriever.setWebScanThreadPool(webScanThreadPool);
        webRetriever.deleteScannedUrls();

        var jobDispatcher = new JobDispatcher(jobQueue, fileScanThreadPool, webScanThreadPool);
        var jobDispatcherThread = new Thread(jobDispatcher);
        jobDispatcherThread.start();

        Scanner scanner = new Scanner(System.in);
        boolean shutdown = false;

        while (!shutdown) {
            String userInput = scanner.nextLine();
            if (userInput.startsWith("ad")) {
                String[] command = userInput.split(" ");
                if (command.length > 2) {
                    System.out.println("Invalid command. Command ad is of the form: ad name_of_directory");
                } else {
                    String directoryPath = command[1];
                    File directory = new File(directoryPath);
                    if (!directory.exists()) {
                        System.out.println("The directory does not exists!");
                    } else if (!directory.isDirectory()) {
                        System.out.println("The file is not a directory. You must specify a path to directory!");
                    }
                    else directoryCrawler.setDirectory(directory);
                }
            }
            else if (userInput.startsWith("aw")) {
                String[] command = userInput.split(" ");
                if (command.length > 2) {
                    System.out.println("Invalid command. Command aw is of the form : aw url");
                } else {
                    String urlString = command[1];
                    try {
                        new URL(command[1]);
                        jobQueue.put(new WebJob(ScanType.WEB, urlString, JobStatus.RUNNING, hop_count));
                    } catch (MalformedURLException | InterruptedException e) {
                        System.out.println(urlString + " is not a valid URL.");
                    }
                }
            }
            else if (userInput.startsWith("get file|summary")) {
                Map<String, Map<String, Integer>> result = fileRetriever.getSummary();
                if (result != null && !result.isEmpty())
                    System.out.println(result);
            }
            else if (userInput.startsWith("query file|summary")) {
                Map<String, Map<String, Integer>> result = fileRetriever.querySummary();
                if (result != null && !result.isEmpty())
                    System.out.println(result);
            }
            else if (userInput.startsWith("get file|")) {
                String directoryPath = userInput.split("\\|")[1];
                Map<String, Integer> result = fileRetriever.getResult("file|" + directoryPath);
                if (!result.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : result.entrySet()) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    }
                }
            }
            else if (userInput.startsWith("query file|")) {
                String directoryPath = userInput.split("\\|")[1];
                Map<String, Integer> result = fileRetriever.getQueryResult("file|" + directoryPath);
                if (!result.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : result.entrySet()) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    }
                }
            }
            else if (userInput.startsWith("get web|summary")) {
                Map<String, Map<String, Integer>> result = webRetriever.getSummary();
                if (result != null && !result.isEmpty())
                    System.out.println(result);
            }
            else if (userInput.startsWith("query web|summary")) {
                Map<String, Map<String, Integer>> result = webRetriever.querySummary();
                if (result != null && !result.isEmpty())
                    System.out.println(result);
            }
            else if (userInput.startsWith("get web|")) {
                String url = userInput.split("\\|")[1];
                Map<String, Integer> result = webRetriever.getResult("web|" + url);
                if (!result.isEmpty()) {
                    System.out.println(result);
                }
            }
            else if (userInput.startsWith("query web|")) {
                String url = userInput.split("\\|")[1];
                Map<String, Integer> result = webRetriever.getQueryResult("web|" + url);
                if (!result.isEmpty()) {
                    System.out.println(result);
                }
            }
            else if (userInput.startsWith("cfs")) {
                fileRetriever.clearSummary();
            }
            else if (userInput.startsWith("cws")) {
                webRetriever.clearSummary();
            }
            else if (userInput.equals("stop")) {
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
                fileRetriever.stopPool();
                webRetriever.stopPool();
            }
            else {
                System.out.println("Command does not exists");
            }
        }
    }
}