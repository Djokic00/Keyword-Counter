package main.application;

import main.crawler.DirectoryCrawler;
import main.job.Job;
import main.dispatcher.JobDispatcher;
import main.result.ResultRetriever;
import main.scanners.FileScanThreadPool;
import main.scanners.WebScanThreadPool;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        var jobQueue = new LinkedBlockingQueue<Job>();
        Properties props = new Properties();
        try {
            InputStream input = new FileInputStream("src/resources/app.properties");
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String allKeywords = props.getProperty("keywords");
        List<String> keywords = Arrays.stream(allKeywords.split(",")).toList();
        String file_corpus_prefix = props.getProperty("file_corpus_prefix");
        int dir_crawler_sleep_time = Integer.parseInt(props.getProperty("dir_crawler_sleep_time"));
        int file_scanning_size_limit = Integer.parseInt(props.getProperty("file_scanning_size_limit"));
        int hop_count = Integer.parseInt(props.getProperty("hop_count"));
        long url_refresh_time = Long.parseLong(props.getProperty("url_refresh_time"));

    }
}