package main.job;

import main.enums.JobStatus;
import main.enums.ScanType;

public class Job implements ScanningJob {
    private ScanType scanType;
    private String query;
    private JobStatus jobStatus;

    public Job(ScanType scanType, String query, JobStatus jobStatus) {
        this.scanType = scanType;
        this.query = query;
        this.jobStatus = jobStatus;
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
    public JobStatus getJobStatus() {
        return jobStatus;
    }

}
