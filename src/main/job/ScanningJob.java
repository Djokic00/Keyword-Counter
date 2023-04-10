package main.job;

import main.enums.CurrentStatus;
import main.enums.ScanType;

public interface ScanningJob {
    ScanType getType();
    String getQuery();
    CurrentStatus getCurrentStatus();
}
