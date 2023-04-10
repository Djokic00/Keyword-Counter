package main.scanners;

import main.job.ScanningJob;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class WebScanThreadPool implements ScanThreadPool {
    ExecutorService pool = Executors.newCachedThreadPool();
    int hopCount;
    long urlRefreshRate;
    List<String> keywords;

    public WebScanThreadPool(int hopCount, long urlRefreshRate, List<String> keywords) {
        this.hopCount = hopCount;
        this.urlRefreshRate = urlRefreshRate;
        this.keywords = keywords;
    }

    @Override
    public void scheduleJob(ScanningJob job) {
        if (!job.isRunning()) {
            System.out.println("Usao u break u File Scanner");
            pool.shutdown();
            return;
        }
        String url = job.getQuery();

        System.out.println();
        System.out.println("Schedule Job in Web");
        System.out.println("---------------------");

        Future<Map<String, Integer>> totalOccurrencesFuture = pool.submit(new WebProcessor(hopCount, urlRefreshRate, url, keywords));

    }
}
