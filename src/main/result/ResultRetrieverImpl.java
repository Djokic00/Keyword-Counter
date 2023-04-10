package main.result;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class ResultRetriever implements Runnable {
    private final ConcurrentHashMap<String, Integer> fileResults;
    private final ConcurrentHashMap<String, Integer> webResults;
    private final ThreadPoolExecutor executor;

    public ResultRetriever(int numThreads) {
        this.fileResults = new ConcurrentHashMap<>();
        this.webResults = new ConcurrentHashMap<>();
        this.executor = new ThreadPoolExecutor(
                numThreads, numThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }


    @Override
    public void run() {

    }
}



    public void addFileResult(String query, int result) {
        fileResults.put(query, result);
    }

    public void addWebResult(String query, int result) {
        webResults.put(query, result);
    }

    public int getFileResult(String query) {
        return fileResults.getOrDefault(query, 0);
    }

    public int getWebResult(String query) {
        return webResults.getOrDefault(query, 0);
    }

    public void processQuery(Query query) {
        switch (query.getType()) {
            case FILE:
                String fileQuery = query.getQuery();
                int fileResult = // get result for file query
                        addFileResult(fileQuery, fileResult);
                break;
            case WEB:
                String webQuery = query.getQuery();
                int webResult = // get result for web query
                        addWebResult(webQuery, webResult);
                break;
            case FILE_SUMMARY:
                executor.execute(() -> {
                    // calculate file summary
                    // add result to fileResults
                });
                break;
            case WEB_SUMMARY:
                executor.execute(() -> {
                    // calculate web summary
                    // add result to webResults
                });
                break;
        }
    }

    public int getQueryResult(Query query) {
        switch (query.getType()) {
            case FILE:
                return getFileResult(query.getQuery());
            case WEB:
                return getWebResult(query.getQuery());
            case FILE_SUMMARY:
            case WEB_SUMMARY:
                // wait for query to finish
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // get result for summary query
                return // summary query result;
            default:
                return 0;
        }
    }

}

public enum QueryType {
    FILE,
    WEB,
    FILE_SUMMARY,
    WEB_SUMMARY
}

public class Query {
    private final QueryType type;
    private final String query;

    public Query(QueryType type, String query) {
        this.type = type;
        this.query = query;
    }

    public QueryType getType() {
        return type;
    }



