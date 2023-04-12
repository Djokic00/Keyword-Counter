package main.dispatcher;

import main.enums.JobStatus;
import main.job.ScanningJob;
import main.enums.ScanType;
import main.job.WebJob;
import main.scanners.file_scanner.FileScanThreadPool;
import main.scanners.web_scanner.WebScanThreadPool;
import java.util.concurrent.*;

public class JobDispatcher implements Runnable {
    private final BlockingQueue<ScanningJob> jobQueue;
    private final FileScanThreadPool fileThreadPool;
    private final WebScanThreadPool webThreadPool;
    private volatile boolean isRunning = true;
    public JobDispatcher(LinkedBlockingQueue<ScanningJob> jobQueue, FileScanThreadPool fileThreadPool, WebScanThreadPool webThreadPool) {
        this.jobQueue = jobQueue;
        this.fileThreadPool = fileThreadPool;
        this.webThreadPool = webThreadPool;
    }

    @Override
    public void run() {
        while (true) {
            ScanningJob job;
            try {
                job = jobQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (!isRunning) {
                job.setJobStatus(JobStatus.STOPPED);
                fileThreadPool.scheduleJob(job);
                WebJob webJob = new WebJob(ScanType.WEB, "", JobStatus.STOPPED, 0);
                webThreadPool.scheduleJob(webJob);
                System.out.println("JobDispatcher is shutting down");
                break;
            }
            else {
                if (job.getType() == ScanType.FILE) {
                    fileThreadPool.scheduleJob(job);
                } else if (job.getType() == ScanType.WEB) {
                    webThreadPool.scheduleJob(job);
                }
            }
        }
    }
    public void stopThread() {
        isRunning = false;
    }
}