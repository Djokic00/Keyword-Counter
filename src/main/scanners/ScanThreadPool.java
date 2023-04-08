package main.scanners;

import main.job.ScanningJob;

public interface ScanThreadPool {
    void scheduleJob(ScanningJob job);
}
