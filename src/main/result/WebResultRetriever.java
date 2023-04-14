package main.result;

import main.enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public class WebResultRetriever implements ResultRetriever {
//    @Override
//    public void addResult(String path, Future<Map<String, Map<String, Integer>>> futureResult) {
//
//    }

    @Override
    public Map<String, Integer> getResult(String query) {
        return null;
    }

    @Override
    public Map<String, Integer> getQueryResult(String query) {
        return null;
    }

    @Override
    public void clearSummary(ScanType summaryType) {

    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        return null;
    }
}
