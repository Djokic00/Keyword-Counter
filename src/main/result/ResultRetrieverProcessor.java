package main.result;

import main.enums.ScanType;

import java.util.Map;
import java.util.concurrent.*;

public class ResultRetrieverImpl implements Runnable, ResultRetriever {
    private final ConcurrentHashMap<String, Integer> fileResults;
    private final ConcurrentHashMap<String, Integer> webResults;
//    private final ThreadPoolExecutor executor;

    BlockingQueue<Future<Map<String, Integer>>> resultQueue;

    public ResultRetrieverImpl(BlockingQueue<Future<Map<String, Integer>>> resultQueue) {
        this.fileResults = new ConcurrentHashMap<>();
        this.webResults = new ConcurrentHashMap<>();
//        this.executor = new ThreadPoolExecutor();
        this.resultQueue = resultQueue;
    }

    @Override
    public void run() {
//        while (true) {
//            try {
//                Future<Map<String, Integer>> future = resultQueue.take();
//                if (future.isDone()) {
//                    Map<String, Integer> result = future.get();
//                    // Do something with the result
//                    // ...
//                } else {
//                    resultQueue.put(future);
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                // Handle exceptions
//                // ...
//            }
//        }
    }

    @Override
    public Map<String, Integer> getResult(String query) {
        Map<String, Integer> result = null;
        try {
            Future<Map<String, Integer>> future = resultQueue.take();
            if (future.isDone()) {
                result = future.get();
            } else {
                resultQueue.put(future);
            }
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions
        }
        return result;
    }

    @Override
    public Map<String, Integer> queryResult(String query) {
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

    @Override
    public void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult) {

    }
}

//    public void processQuery(Query query) {
//        switch (query.getType()) {
//            case FILE:
//                String fileQuery = query.getQuery();
//                int fileResult = // get result for file query
//                        addFileResult(fileQuery, fileResult);
//                break;
//            case WEB:
//                String webQuery = query.getQuery();
//                int webResult = // get result for web query
//                        addWebResult(webQuery, webResult);
//                break;
//            case FILE_SUMMARY:
//                executor.execute(() -> {
//                    // calculate file summary
//                    // add result to fileResults
//                });
//                break;
//            case WEB_SUMMARY:
//                executor.execute(() -> {
//                    // calculate web summary
//                    // add result to webResults
//                });
//                break;
//        }
//    }
//
//    public int getQueryResult(Query query) {
//        switch (query.getType()) {
//            case FILE:
//                return getFileResult(query.getQuery());
//            case WEB:
//                return getWebResult(query.getQuery());
//            case FILE_SUMMARY:
//            case WEB_SUMMARY:
//                // wait for query to finish
//                executor.shutdown();
//                try {
//                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                // get result for summary query
//                return // summary query result;
//            default:
//                return 0;
//        }
//    }




