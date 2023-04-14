package main.crawler;

import main.application.Config;
import main.enums.JobStatus;
import main.job.FileJob;
import main.enums.ScanType;
import main.job.ScanningJob;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class DirectoryCrawler implements Runnable {
    private final Map<File, Long> lastModifiedMap = new HashMap<>();
    private List<File> textCorpora = new ArrayList<>();
    private LinkedBlockingQueue<ScanningJob> jobQueue;
    private File lastModifiedFile;
    private File directory = null;
    private final Semaphore semaphore = new Semaphore(1);
    private volatile File newDirectory = null;
    private volatile boolean isRunning = true;

    public DirectoryCrawler(LinkedBlockingQueue<ScanningJob> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!isRunning) {
                    jobQueue.put(new FileJob(ScanType.FILE, "", JobStatus.STOPPED));
                    break;
                }
                semaphore.acquire();

                if (newDirectory != null) {
                    directory = newDirectory;
                    newDirectory = null;
                }

                semaphore.release();

                if (directory != null) {
                    List<File> textCorpora = findTextCorpora(directory);
                    for (File corpusDir : textCorpora) {
                        Long lastModified = getLastModified(corpusDir);
                        Long previousModified = lastModifiedMap.get(lastModifiedFile);
                        if (previousModified == null || lastModified > previousModified) {
                            try {
                                jobQueue.put(new FileJob(ScanType.FILE, corpusDir.getAbsolutePath(), JobStatus.RUNNING));
                                lastModifiedMap.put(lastModifiedFile, lastModified);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            lastModifiedMap.put(corpusDir, lastModified);
                        }
                    }
                }

                Thread.sleep(Config.getInstance().dir_crawler_sleep_time);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<File> findTextCorpora(File rootDirectory) {
        if (rootDirectory.isDirectory()) {
            for (File file : rootDirectory.listFiles()) {
                if (file.isDirectory()) {
                    if (file.getName().startsWith(Config.getInstance().file_corpus_prefix)) {
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

    public void setDirectory(File directory) {
        try {
            semaphore.acquire();
            newDirectory = directory;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
    }

    public void stopThread() {
        isRunning = false;
    }
}

