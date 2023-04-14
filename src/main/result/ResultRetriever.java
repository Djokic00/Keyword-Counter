package main.result;

import main.enums.ScanType;

import java.util.Map;
import java.util.concurrent.Future;

public interface ResultRetriever {
    void addResult(String path, Future<Map<String, Map<String, Integer>>> futureResult);
    Map<String, Integer> getResult(String query);
    Map<String, Integer> getQueryResult(String query);
    void clearSummary();
    Map<String, Map<String, Integer>> getSummary();
    Map<String, Map<String, Integer>> querySummary();

}
