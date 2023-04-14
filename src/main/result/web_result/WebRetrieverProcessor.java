package main.result.web_result;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class WebRetrieverProcessor implements Callable {
    private final ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> webResults;

    public WebRetrieverProcessor(ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> webResults) {
        this.webResults = webResults;
    }

    @Override
    public Object call() {
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
}
