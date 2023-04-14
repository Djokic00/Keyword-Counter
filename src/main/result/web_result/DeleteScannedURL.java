package main.result.web_result;

import main.scanners.web_scanner.WebScanThreadPool;

public class DeleteScannedURL implements Runnable {
    private WebRetriever webRetriever;
    private WebScanThreadPool webScanThreadPool;

    public DeleteScannedURL(WebRetriever webRetriever, WebScanThreadPool webScanThreadPool) {
        this.webRetriever = webRetriever;
        this.webScanThreadPool = webScanThreadPool;
    }

    @Override
    public void run() {
//        System.out.println("Brisanje rezultata");
        webRetriever.setResultSummaryCacheWeb(null);
        webRetriever.webResults.clear();
        webScanThreadPool.processedLinks.clear();
    }
}
