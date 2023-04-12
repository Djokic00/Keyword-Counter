package main.scanners.file_scanner;

import main.enums.JobStatus;
import main.job.FileJob;
import main.job.ScanningJob;
import main.result.ResultRetrieverThreadPool;
import main.scanners.ScanThreadPool;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileScanThreadPool implements ScanThreadPool {
    private final int corpusSizeLimit;
    private final List<String> keywords;
    private ResultRetrieverThreadPool retrieverThreadPool;
    ForkJoinPool pool;

    public FileScanThreadPool(int corpusSizeLimit, List<String> keywords, ResultRetrieverThreadPool retrieverThreadPool) {
        this.corpusSizeLimit = corpusSizeLimit;
        this.keywords = keywords;
        this.retrieverThreadPool = retrieverThreadPool;
        pool = new ForkJoinPool();
    }

    @Override
    public void scheduleJob(ScanningJob job) {
        FileJob fileJob = (FileJob) job;
        if (fileJob.getJobStatus() == JobStatus.STOPPED) {
            System.out.println("Usao u break u File Scanner");
            pool.shutdown();
            return;
        }
        String directoryPath = fileJob.getQuery();
        File directory = new File(directoryPath);
        long directorySize = getDirectorySize(directory);

        File[] listFiles = directory.listFiles();

        System.out.println();
        System.out.println("Schedule Job");
        System.out.println("Velicina fajla "  + directorySize);
        System.out.println("---------------------");
        Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture = pool.submit(new FileProcessor(0, directorySize, corpusSizeLimit, listFiles, keywords));


        System.out.println("Filescan: " + directoryPath);
        retrieverThreadPool.addFileResult(directory.getName(), totalOccurrencesFuture);
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

}

