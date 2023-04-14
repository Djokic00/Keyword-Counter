package main.result;

import main.scanners.web_scanner.WebScanThreadPool;

public class DeleteScannedURL implements Runnable {
    private ResultRetrieverThreadPool retrieverThreadPool;
    private WebScanThreadPool webScanThreadPool;

    public DeleteScannedURL(ResultRetrieverThreadPool retrieverThreadPool, WebScanThreadPool webScanThreadPool) {
        this.retrieverThreadPool = retrieverThreadPool;
        this.webScanThreadPool = webScanThreadPool;
    }

    @Override
    public void run() {
//        System.out.println("Brisanje rezultata");
        retrieverThreadPool.setResultSummaryCacheWeb(null);
        retrieverThreadPool.webResults.clear();
        webScanThreadPool.processedLinks.clear();
    }
}
