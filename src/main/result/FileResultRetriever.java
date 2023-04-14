package main.result;

import main.enums.ScanType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileResultRetriever implements ResultRetriever {
    private Future<Map<String, Map<String, Integer>>> resultSummaryCacheFile;
    private final ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults;

    public FileResultRetriever(ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults) {
        this.fileResults = fileResults;
    }

//    @Override
//    public void addResult(String directoryPath, Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture) {
//        resultSummaryCacheFile = null;
//        fileResults.put(directoryPath, totalOccurrencesFuture);
//    }

    @Override
    public Map<String, Integer> getResult(String query) {
        String scanType = query.split("\\|")[0];
        String path = query.split("\\|")[1];
        Map<String, Integer> result = new HashMap<>();
        if (scanType.equals("file")) {
            try {
                Future<Map<String, Map<String, Integer>>> future = fileResults.get(path);
                result = future.get().get(path);
            } catch (InterruptedException | ExecutionException e) {

            }
        }
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
