package main.result;

import main.enums.ScanType;
import main.scanners.web_scanner.WebScanThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ResultRetrieverThreadPool implements ResultRetriever {
    private ExecutorService pool;
    private Future<Map<String, Map<String, Integer>>> resultSummaryCacheFile;
    public Future<Map<String, Map<String, Integer>>> resultSummaryCacheWeb;
    public ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults;
    public ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> webResults;
    private ScheduledExecutorService executor;
    private WebScanThreadPool webScanThreadPool;
    private long url_refresh_time;

    public ResultRetrieverThreadPool(long url_refresh_time) {
        this.fileResults = new ConcurrentHashMap<>();
        this.webResults = new ConcurrentHashMap<>();
        this.pool = Executors.newCachedThreadPool();
        this.executor = Executors.newScheduledThreadPool(5);
        this.url_refresh_time = url_refresh_time;
//        deleteScannedUrls();
    }

    public void addFileResult(String directoryPath, Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture) {
        resultSummaryCacheFile = null;
        fileResults.put(directoryPath, totalOccurrencesFuture);
    }

    public void addWebResult(String url, Future<Map<String, Map<String, Integer>>> totalOccurrencesFuture) {
        resultSummaryCacheWeb = null;
        webResults.put(url, totalOccurrencesFuture);
    }

    public void setWebScanThreadPool(WebScanThreadPool webScanThreadPool) {
        this.webScanThreadPool = webScanThreadPool;
    }

    public void deleteScannedUrls() {
        DeleteScannedURL deleteScannedURL = new DeleteScannedURL(this, webScanThreadPool);
        executor.scheduleWithFixedDelay(deleteScannedURL, url_refresh_time,  url_refresh_time, TimeUnit.MILLISECONDS);
    }

    @Override
    public Map<String, Integer> getResult(String query) {
        String scanType = query.split("\\|")[0];
        String path = query.split("\\|")[1];
        Map<String, Integer> result = new HashMap<>();
        try {
            if (scanType.equals("file")) {
                if (fileResults.get(path) != null) {
                    Future<Map<String, Map<String, Integer>>> future = fileResults.get(path);
                    result = future.get().get(path);
                }
                else System.out.println("File with that name is not processed yet");
            }
            else if (scanType.equals("web")) {
                if (webResults.get(path) != null) {
                    Future<Map<String, Map<String, Integer>>> future = webResults.get(path);
                    result = future.get().get(path);
                }
                else System.out.println("Page with that name is not processed yet");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, Integer> getQueryResult(String query) {
        String scanType = query.split("\\|")[0];
        String path = query.split("\\|")[1];
        Map<String, Integer> result = new HashMap<>();
        try {
            if (scanType.equals("file")) {
                if (fileResults.get(path) != null) {
                    Future<Map<String, Map<String, Integer>>> future = fileResults.get(path);
                    if (future.isDone()) {
                        result = future.get().get(path);
                    } else {
                        System.out.println("Task is not done yet.");
                    }
                }
            }
            else if (scanType.equals("web")) {
                Future<Map<String, Map<String, Integer>>> future = webResults.get(path);
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
    public void clearSummary(ScanType summaryType) {
        if (summaryType.equals(ScanType.FILE)) resultSummaryCacheFile = null;
        if (summaryType.equals(ScanType.WEB)) resultSummaryCacheWeb = null;
    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType)  {
        try {
            if (summaryType.equals(ScanType.FILE)) {
                if (resultSummaryCacheFile == null) {
                    resultSummaryCacheFile = pool.submit(new ResultRetrieverProcessor(summaryType, fileResults, webResults));
                }
                return resultSummaryCacheFile.get();
            }
            else if (summaryType.equals(ScanType.WEB)) {
                if (webResults.isEmpty()) {
                    System.out.println("Web is empty");
                    return null;
                }
                if (resultSummaryCacheWeb == null) {
                    System.out.println("Racuna");
                    resultSummaryCacheWeb = pool.submit(new ResultRetrieverProcessor(summaryType, fileResults, webResults));
                }
                System.out.println("Vraca kesirano");
                return resultSummaryCacheWeb.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        try {
            if (summaryType.equals(ScanType.FILE)) {
                if (resultSummaryCacheFile == null || !resultSummaryCacheFile.isDone()) {
                    resultSummaryCacheFile = pool.submit(new ResultRetrieverProcessor(summaryType, fileResults, webResults));
                    System.out.println("Summary is not ready yet...");
                } else {
                    return resultSummaryCacheFile.get();
                }
            } else if (summaryType.equals(ScanType.WEB)) {
                if (resultSummaryCacheWeb == null || !resultSummaryCacheWeb.isDone()) {
                    resultSummaryCacheWeb = pool.submit(new ResultRetrieverProcessor(summaryType, fileResults, webResults));
                    System.out.println("Summary is not ready yet...");
                } else {
                    return resultSummaryCacheWeb.get();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopPool() {
        executor.shutdown();
        pool.shutdown();
    }

    public void setResultSummaryCacheWeb(Future<Map<String, Map<String, Integer>>> resultSummaryCacheWeb) {
        this.resultSummaryCacheWeb = resultSummaryCacheWeb;
    }
}