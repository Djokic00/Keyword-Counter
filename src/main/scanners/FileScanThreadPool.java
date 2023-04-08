package main.scanners;

import main.job.ScanningJob;
import main.result.ResultRetriever;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileScanThreadPool implements ScanThreadPool, Runnable {
    private final int corpusSizeLimit;
    private final List<String> keywords;
    private final ResultRetriever resultRetriever;
    ForkJoinPool pool;

    public FileScanThreadPool(int corpusSizeLimit, List<String> keywords, ResultRetriever resultRetriever) {
        this.corpusSizeLimit = corpusSizeLimit;
        this.keywords = keywords;
        this.resultRetriever = resultRetriever;
    }

    @Override
    public void run() {
         pool = new ForkJoinPool();
    }

    @Override
    public void scheduleJob(ScanningJob job) {
        String directoryPath = job.getQuery();
        File directory = new File(directoryPath);
//        long directorySize = getDirectorySize(directory);

        File[] listFiles = directory.listFiles();

        System.out.println("Schedule Job");
        Future<Map<String, Integer>> totalOccurrencesFuture = pool.submit(new FileProcessor(corpusSizeLimit, listFiles, keywords));

        try {
            Map<String, Integer> totalOccurrences = totalOccurrencesFuture.get();
            for (Map.Entry<String, Integer> entry : totalOccurrences.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

