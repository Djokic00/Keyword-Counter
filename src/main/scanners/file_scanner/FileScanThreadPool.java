package main.scanners;

import main.job.ScanningJob;
import main.result.ResultRetriever;
import main.result.ResultRetrieverImpl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileScanThreadPool implements ScanThreadPool {
    private final int corpusSizeLimit;
    private final List<String> keywords;
    private final BlockingQueue<Future<Map<String, Integer>>> resultQueue;
    ForkJoinPool pool;

    public FileScanThreadPool(int corpusSizeLimit, List<String> keywords, BlockingQueue<Future<Map<String, Integer>>> resultQueue) {
        this.corpusSizeLimit = corpusSizeLimit;
        this.keywords = keywords;
        this.resultQueue = resultQueue;
        pool = new ForkJoinPool();
    }

    @Override
    public void scheduleJob(ScanningJob job) {
        if (!job.isRunning()) {
            System.out.println("Usao u break u File Scanner");
            pool.shutdown();
            return;
        }
        String directoryPath = job.getQuery();
        File directory = new File(directoryPath);
        long directorySize = getDirectorySize(directory);

        File[] listFiles = directory.listFiles();

        System.out.println();
        System.out.println("Schedule Job");
        System.out.println("Velicina fajla "  + directorySize);
        System.out.println("---------------------");
        Future<Map<String, Integer>> totalOccurrencesFuture = pool.submit(new FileProcessor(0, directorySize, corpusSizeLimit, listFiles, keywords));


        try {
            Map<String, Integer> mapa = totalOccurrencesFuture.get();
            for (Map.Entry<String, Integer> entry : mapa.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
            resultQueue.put(totalOccurrencesFuture);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private long getDirectorySize(File directory) {
        long numberOfBytes = 0;
        if (directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (!file.isDirectory())
                        numberOfBytes += file.length();
                    if (file.isDirectory()) {
                        numberOfBytes += getDirectorySize(file);
                    }
                }
            }
        } else {
            numberOfBytes += directory.length();
        }
        return numberOfBytes;
    }

//    public void stopThread() {
//        isRunning.set(false);
//    }
}

