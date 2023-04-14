package main.scanners.web_scanner;

import main.enums.JobStatus;
import main.job.ScanningJob;
import main.job.WebJob;
import main.result.ResultRetrieverThreadPool;
import main.scanners.ScanThreadPool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebScanThreadPool implements ScanThreadPool {
    private ExecutorService pool = Executors.newCachedThreadPool();
    public ConcurrentHashMap<String, Boolean> processedLinks = new ConcurrentHashMap<>();
    BlockingQueue jobQueue;
    private ResultRetrieverThreadPool retrieverThreadPool;

    private List<String> keywords;

    public WebScanThreadPool(BlockingQueue jobQueue, ResultRetrieverThreadPool retrieverThreadPool, List<String> keywords) {
        this.jobQueue = jobQueue;
        this.retrieverThreadPool = retrieverThreadPool;
        this.keywords = keywords;
    }
    @Override
    public void scheduleJob(ScanningJob job) {
        WebJob webJob = (WebJob) job;
        if (webJob.getJobStatus() == JobStatus.STOPPED) {
            System.out.println("Web scanner is shutting down");
            pool.shutdown();
            return;
        }
        String url = webJob.getQuery();
        int hopCount = webJob.getHopCount();

        Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture = pool.submit(new WebProcessor(jobQueue, this, hopCount, url, keywords ));

        retrieverThreadPool.addWebResult(url, totalOccurrencesFuture);
    }
}