package main.crawler;

import main.job.Job;
import main.enums.ScanType;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectoryCrawler implements Runnable {

    private final String directoryPrefix;
    private final long dirCrawlerSleepTime;
    private final Map<File, Long> lastModifiedMap = new HashMap<>();
    private List<File> textCorpora = new ArrayList<>();
    private LinkedBlockingQueue<Job> jobQueue;
    private File lastModifiedFile;

    private File directory;

    public DirectoryCrawler(String directoryPrefix, long dirCrawlerSleepTime, LinkedBlockingQueue<Job> jobQueue, File directory) {
        this.directoryPrefix = directoryPrefix;
        this.dirCrawlerSleepTime = dirCrawlerSleepTime;
        this.jobQueue = jobQueue;
        this.directory = directory;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            textCorpora = new ArrayList<>();
            List<File> textCorpora = findTextCorpora(directory);

            for (File corpusDir : textCorpora) {
                Long lastModified = getLastModified(corpusDir);
                Long previousModified = lastModifiedMap.get(lastModifiedFile);

                System.out.println("Last: " + lastModified + " " + "Prev: " + previousModified);

                if (previousModified == null || lastModified > previousModified) {
                    try {
                        jobQueue.put(new Job(ScanType.FILE, corpusDir.getAbsolutePath()));
                        lastModifiedMap.put(lastModifiedFile, lastModified);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    lastModifiedMap.put(corpusDir, lastModified);
                }
            }

            // Sleep for the specified interval before continuing the search
            try {
                Thread.sleep(dirCrawlerSleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<File> findTextCorpora(File rootDirectory) {
        if (rootDirectory.isDirectory()) {
            for (File file : rootDirectory.listFiles()) {
                if (file.isDirectory()) {
                    if (file.getName().startsWith(directoryPrefix)) {
                        textCorpora.add(file);
                        findTextCorpora(file);
                    }
                    findTextCorpora(file);
                }
            }
        }
        return textCorpora;
    }

    private Long getLastModified(File directory) {
        Long lastModified = 0L;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                long fileLastModified = file.lastModified();
                if (fileLastModified > lastModified) {
                    lastModified = fileLastModified;
                    this.lastModifiedFile = file;
                }
            }
        }
        return lastModified;
    }
}

