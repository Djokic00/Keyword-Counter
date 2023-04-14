package main.result.file_result;

import main.enums.ScanType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class FileRetrieverProcessor implements Callable {

    private final ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults;

    public FileRetrieverProcessor(ConcurrentHashMap<String, Future<Map<String, Map<String, Integer>>>> fileResults) {
        this.fileResults = fileResults;
    }

    @Override
    public Object call() {
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
}
