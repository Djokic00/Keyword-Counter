package main.result.file_result;

import main.result.ResultRetriever;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class FileRetriever implements ResultRetriever {
    private ExecutorService pool;
    private Future<Map<String, Map<String, Integer>>> resultSummaryCacheFile;
    public ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults;

    public FileRetriever() {
        this.fileResults = new ConcurrentHashMap<>();
        this.pool = Executors.newCachedThreadPool();
    }

    @Override
    public void addResult(String directoryPath, Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture) {
        resultSummaryCacheFile = null;
        fileResults.put(directoryPath, totalOccurrencesFuture);
    }

    @Override
    public Map<String, Integer> getResult(String query) {
        String path = query.split("\\|")[1];
        Map<String, Integer> result = new HashMap<>();
        try {
            if (fileResults.get(path) != null) {
                Future<Map<String, Map<String, Integer>>> future = fileResults.get(path);
                result = future.get().get(path);
            }
            else System.out.println("File with that name is not processed yet");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, Integer> getQueryResult(String query) {
        String path = query.split("\\|")[1];
        Map<String, Integer> result = new HashMap<>();
        try {
            if (fileResults.get(path) != null) {
                Future<Map<String, Map<String, Integer>>> future = fileResults.get(path);
                if (future.isDone()) {
                    result = future.get().get(path);
                } else {
                    System.out.println("Task is not done yet.");
                }
            }
        } catch (InterruptedException | ExecutionException e) {

        }
        return result;
    }

    @Override
    public void clearSummary() {
        resultSummaryCacheFile = null;
    }

    @Override
    public Map<String, Map<String, Integer>> getSummary() {
        try {
            if (resultSummaryCacheFile == null) {
                System.out.println("Computing");
                resultSummaryCacheFile = pool.submit(new FileRetrieverProcessor(fileResults));
            }
            System.out.println("Returns cache");
            return resultSummaryCacheFile.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary() {
        try {
            if (resultSummaryCacheFile == null || !resultSummaryCacheFile.isDone()) {
                resultSummaryCacheFile = pool.submit(new FileRetrieverProcessor(fileResults));
                System.out.println("Summary is not ready yet...");
            } else {
                return resultSummaryCacheFile.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopPool() {
        System.out.println("FileRetriever is shutting down");
        pool.shutdown();
    }
}
