package main.scanners.web_scanner;

import main.enums.JobStatus;
import main.enums.ScanType;
import main.job.WebJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class WebProcessor implements Callable {
    private int hopCount;
    private List<String> keywords;
    private String url;
    private WebScanThreadPool webScanThreadPool;

    private BlockingQueue jobQueue;

    public WebProcessor(BlockingQueue jobQueue, WebScanThreadPool webScanThreadPool, int hopCount, String url, List<String> keywords) {
        this.jobQueue = jobQueue;
        this.webScanThreadPool = webScanThreadPool;
        this.hopCount = hopCount;
        this.keywords = keywords;
        this.url = url;
    }

    @Override
    public Object call() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        try {
            Document document = Jsoup.connect(url).get();
            String textContent = document.text();

            System.out.println("Processing url: " + url);
            System.out.println();

            Map<String, Integer> innerMap = new HashMap<>();
            for (String key: keywords) {
                innerMap.put(key, 0);
            }
            result.put(url, innerMap);

            if (hopCount != 0) {
                Elements links = document.select("a[href]");
                for (Element link : links) {
                    webScanThreadPool.processedLinks.computeIfAbsent(link.attr("abs:href"), (key) -> {
                        String url = link.attr("abs:href");
                        try {
                            URL u = new URL(url);
                            String domain = u.getHost();
                            if (domain.isEmpty()) return false;
                            jobQueue.put(new WebJob(ScanType.WEB, link.attr("abs:href"), JobStatus.RUNNING, hopCount - 1));
                            return true;
                        } catch (IOException | InterruptedException e) {

                        }
                        return false;
                    });
                }
            }

            Map<String, Integer> newInnerMap = new HashMap<>();
            for (String keyword: keywords) {
                int count = countKeywordOccurrences(textContent, keyword);
                int currentValue = result.get(url).get(keyword);
                newInnerMap.put(keyword, count + currentValue);
            }
            result.put(url, newInnerMap);

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Url not valid: " + this.url);
            System.out.println();
        }

        return result;
    }

    public int countKeywordOccurrences(String textContent, String keyword) {
        String[] words = textContent.split("\\W+");
        int count = 0;
        for (String word : words) {
            if (word.equalsIgnoreCase(keyword)) {
                count++;
            }
        }
        return count;
    }
}
