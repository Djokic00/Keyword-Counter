package main.job;

import main.enums.JobStatus;
import main.enums.ScanType;

public interface ScanningJob {
    ScanType getType();
    String getQuery();
    JobStatus getJobStatus();
}
