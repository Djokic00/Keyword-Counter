package main.job;

import main.enums.ScanType;

public interface ScanningJob {
    ScanType getType();
    String getQuery();
    boolean isRunning();
}
