package main.job;

import main.enums.JobStatus;
import main.enums.ScanType;

public class WebJob implements ScanningJob {

    private ScanType scanType;
    private String query;
    private JobStatus jobStatus;

    private int hopCount;

    public WebJob(ScanType scanType, String query, JobStatus jobStatus, int hopCount) {
        this.scanType = scanType;
        this.query = query;
        this.jobStatus = jobStatus;
        this.hopCount = hopCount;
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

    @Override
    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public int getHopCount() {
        return hopCount;
    }
}
