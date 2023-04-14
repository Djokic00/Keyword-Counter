package main.result.web_result;

import main.enums.ScanType;
import main.result.ResultRetriever;
import main.scanners.web_scanner.WebScanThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class WebRetriever implements ResultRetriever {
    private ExecutorService pool;
    public Future<Map<String, Map<String, Integer>>> resultSummaryCacheWeb;
    public ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> webResults;
    private ScheduledExecutorService executor;
    private WebScanThreadPool webScanThreadPool;
    private long url_refresh_time;

    public WebRetriever(long url_refresh_time) {
        this.webResults = new ConcurrentHashMap<>();
        this.pool = Executors.newCachedThreadPool();
        this.executor = Executors.newScheduledThreadPool(5);
        this.url_refresh_time = url_refresh_time;
    }

    @Override
    public void addResult(String url, Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture) {
        resultSummaryCacheWeb = null;
        webResults.put(url, totalOccurrencesFuture);
    }

    @Override
    public Map<String, Integer> getResult(String query) {
        String path = query.split("\\|")[1];
        Map<String, Integer> result = new HashMap<>();
        try {
            if (webResults.get(path) != null) {
                Future<Map<String, Map<String, Integer>>> future = webResults.get(path);
                result = future.get().get(path);
            }
            else System.out.println("Page with that name is not processed yet");
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
            Future<Map<String, Map<String, Integer>>> future = webResults.get(path);
            if (future.isDone()) {
                result = future.get().get(path);
            } else {
                System.out.println("Task is not done yet.");
            }
        } catch (InterruptedException | ExecutionException e) {

        }
        return result;
    }

    @Override
    public void clearSummary() {
        resultSummaryCacheWeb = null;
    }

    @Override
    public Map<String, Map<String, Integer>> getSummary() {
        try {
            if (webResults.isEmpty()) {
                System.out.println("Web is empty");
                return null;
            }
            if (resultSummaryCacheWeb == null) {
                System.out.println("Computing");
                resultSummaryCacheWeb = pool.submit(new WebRetrieverProcessor(webResults));
            }
            System.out.println("Returns cache");
            return resultSummaryCacheWeb.get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary() {
        try {
            if (resultSummaryCacheWeb == null || !resultSummaryCacheWeb.isDone()) {
                resultSummaryCacheWeb = pool.submit(new WebRetrieverProcessor(webResults));
                System.out.println("Summary is not ready yet...");
            } else {
                return resultSummaryCacheWeb.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteScannedUrls() {
        DeleteScannedURL deleteScannedURL = new DeleteScannedURL(this, webScanThreadPool);
        executor.scheduleWithFixedDelay(deleteScannedURL, url_refresh_time,  url_refresh_time, TimeUnit.MILLISECONDS);
    }

    public void setResultSummaryCacheWeb(Future<Map<String, Map<String, Integer>>> resultSummaryCacheWeb) {
        this.resultSummaryCacheWeb = resultSummaryCacheWeb;
    }

    public void setWebScanThreadPool(WebScanThreadPool webScanThreadPool) {
        this.webScanThreadPool = webScanThreadPool;
    }

    public void stopPool() {
        System.out.println("WebRetriever is shutting down");
        executor.shutdown();
        pool.shutdown();
    }
}
