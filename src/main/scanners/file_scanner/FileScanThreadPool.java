package main.scanners.file_scanner;

import main.application.Config;
import main.enums.JobStatus;
import main.job.FileJob;
import main.job.ScanningJob;
import main.result.file_result.FileRetriever;
import main.scanners.ScanThreadPool;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileScanThreadPool implements ScanThreadPool {
    private FileRetriever fileRetriever;
    private ForkJoinPool pool;

    private Config config = Config.getInstance();

    public FileScanThreadPool(FileRetriever fileRetriever) {
        this.fileRetriever = fileRetriever;
        pool = new ForkJoinPool();
    }

    @Override
    public void scheduleJob(ScanningJob job) {
        FileJob fileJob = (FileJob) job;
        if (fileJob.getJobStatus() == JobStatus.STOPPED) {
            System.out.println("FileScanner is shutting down");
            pool.shutdown();
            return;
        }
        String directoryPath = fileJob.getQuery();
        File directory = new File(directoryPath);
        long directorySize = getDirectorySize(directory);

        File[] listFiles = directory.listFiles();

        Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture = pool.submit(
                new FileProcessor(0, directorySize, config.file_scanning_size_limit, listFiles, config.keywords));

        System.out.println("File: " + directoryPath + ", file size: " + directorySize);
        fileRetriever.addResult(directory.getName(), totalOccurrencesFuture);
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

