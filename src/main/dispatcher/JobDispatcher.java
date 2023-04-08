package main.dispatcher;

import main.job.ScanningJob;
import main.enums.ScanType;
import main.job.Job;
import main.scanners.FileScanThreadPool;
import main.scanners.WebScanThreadPool;
import java.util.concurrent.*;

public class JobDispatcher implements Runnable {
    private final BlockingQueue<Job> jobQueue;
    private final FileScanThreadPool fileThreadPool;
    private final WebScanThreadPool webThreadPool;

    public JobDispatcher(LinkedBlockingQueue<Job> jobQueue, FileScanThreadPool fileThreadPool, WebScanThreadPool webThreadPool) {
        this.jobQueue = jobQueue;
        this.fileThreadPool = fileThreadPool;
        this.webThreadPool = webThreadPool;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ScanningJob job = jobQueue.take();
                if (job.getType() == ScanType.FILE) {
                    fileThreadPool.scheduleJob(job);
                } else if (job.getType() == ScanType.WEB) {
                    webThreadPool.scheduleJob(job);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}