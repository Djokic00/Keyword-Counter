package main.job;

import main.enums.CurrentStatus;
import main.enums.ScanType;

public class Job implements ScanningJob {
    private ScanType scanType;
    private String query;
    private CurrentStatus currentStatus;

    public Job(ScanType scanType, String query, CurrentStatus currentStatus) {
        this.scanType = scanType;
        this.query = query;
        this.currentStatus = currentStatus;
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
    public CurrentStatus getCurrentStatus() {
        return currentStatus;
    }

}
