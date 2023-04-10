package main.job;

import main.enums.ScanType;

public class Job implements ScanningJob {
    private ScanType scanType;
    private String query;

    private volatile boolean isRunning;

    public Job(ScanType scanType, String query, boolean isRunning) {
        this.scanType = scanType;
        this.query = query;
        this.isRunning = isRunning;
    }

    @Override
    public ScanType getType() {
        return scanType;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

}
