package main.scanners;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

public class FileProcessor extends RecursiveTask {
    private long start;
    private long end;
    private long corpusSizeLimit;
    private File[] files;
    private final List<String> keywords;

    public FileProcessor(long corpusSizeLimit, File[] files, List<String> keywords) {
        this.corpusSizeLimit = corpusSizeLimit;
        this.files = files;
        this.keywords = keywords;
    }

    private Map<String, Integer> countOccurrences(File file, List<String> keywords) throws Exception {
        Map<String, Integer> occurrences = new HashMap<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                for (String keyword : keywords) {
                    occurrences.put(keyword, countKeywordOccurrences(line, keyword));
                }
            }
        }
        return occurrences;
    }

    private int countKeywordOccurrences(String line, String keyword) {
        int occurrences = 0;
        int index = line.indexOf(keyword);
        while (index >= 0) {
            occurrences++;
            index = line.indexOf(keyword, index + keyword.length());
        }
        return occurrences;
    }

    @Override
    protected Object compute() {
        Map<String, Integer> result = new HashMap<>();

        System.out.println(files[0].getParentFile().getName());
        long directorySize = getDirectorySize(files[0].getParentFile());
        // nismo sigurni za ovo
        System.out.println("FILES length: " + files.length);
        if (directorySize <= corpusSizeLimit) {
            System.out.println("File Processor - Usao u if");
            long sum = 0;
            for (File file : files) {
                sum += file.length();
                if (sum > corpusSizeLimit) {
                    break;
                }
                try {
                    Map<String, Integer> map = countOccurrences(file, keywords);
                    for (String key : map.keySet()) {
                        int value1 = map.get(key);
                        int value2 = result.get(key);
                        result.put(key, value1 + value2);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            System.out.println("USAO U ELSE");
            int mid = files.length / 2;

            FileProcessor left = new FileProcessor(corpusSizeLimit, Arrays.copyOfRange(files, 0, mid), keywords);
            FileProcessor right = new FileProcessor(corpusSizeLimit, Arrays.copyOfRange(files, mid, files.length), keywords);

            // ovim pravimo novu nit koja ce da se bavi levim poslom
            left.fork();

            //ova nit koja je i podelila posao racuna svoj, desni deo posla
            Map<String, Integer> rightResult = (Map<String, Integer> ) right.compute();

            //dohvatamo sta je rezultat leve, nove, niti
            Map<String, Integer> leftResult = (Map<String, Integer> ) left.join();

            for (String key : rightResult.keySet()) {
                int value1 = rightResult.get(key);
                int value2 = leftResult.get(key);
                result.put(key, value1 + value2);
            }
        }
        return result;
    }

    private long getDirectorySize(File directory) {
        long numberOfBytes = 0;
        if (directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (!file.isDirectory())
                        numberOfBytes += file.length();
                    if (file.isDirectory()) {
                        numberOfBytes += getDirectorySize(file);
                    }
                }
            }
        } else {
            numberOfBytes += directory.length();
        }
        return numberOfBytes;
    }
}