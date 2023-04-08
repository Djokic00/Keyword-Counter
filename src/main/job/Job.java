package main.job;

import main.enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public class Job implements ScanningJob {
    private ScanType scanType;
    private String query;

    public Job(ScanType scanType, String query) {
        this.scanType = scanType;
        this.query = query;
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
    public Future<Map<String, Integer>> initiate() {
        return null;
    }

}
