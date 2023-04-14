package main.result;

import main.enums.ScanType;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class ResultRetrieverProcessor implements Callable {
    private ScanType scanType;
    private final ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults;
    private final ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> webResults;

    public ResultRetrieverProcessor(ScanType scanType, ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults,
                                    ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> webResults) {
        this.scanType = scanType;
        this.fileResults = fileResults;
        this.webResults = webResults;
    }

    public Map<String, Map<String, Integer>> summaryFile(){
        Map<String, Map<String, Integer>> result = new HashMap<>();
        for (String key: fileResults.keySet()) {
            Future<Map<String, Map<String, Integer>>> future = fileResults.get(key);
            try {
                result.put(key, future.get().get(key));
            } catch (Exception e) {

            }
        }
        return result;
    }

    public Map<String, Map<String, Integer>> summaryWeb() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        for (String key: webResults.keySet()) {
            try {
                URL u = new URL(key);
                String domain = u.getHost();
                Map<String, Integer> innerMap = new HashMap<>();
                result.put(domain, innerMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String key: webResults.keySet()) {
            try {
                URL u = new URL(key);
                String domain = u.getHost();

                Future<Map<String, Map<String, Integer>>> future = webResults.get(key);
                Map<String, Integer> newMap = future.get().get(key);
                Map<String, Integer> oldMap = result.get(domain);

                for (String entry : oldMap.keySet()) {
                    int valueFromFirstMap = oldMap.get(entry);
                    int valueFromSecondMap = newMap.get(entry);
                    newMap.put(entry, valueFromFirstMap + valueFromSecondMap);
                }
                result.put(domain, newMap);
            } catch (Exception e) {

            }
        }
        return result;
    }


    @Override
    public Object call() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        if (scanType.equals(ScanType.WEB)) {
            result = this.summaryWeb();
            return result;
        }
        if (scanType.equals(ScanType.FILE)) {
            result =  this.summaryFile();
            return result;
        }
        return result;
    }
}
