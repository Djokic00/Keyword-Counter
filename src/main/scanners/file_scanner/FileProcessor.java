package main.scanners;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

public class FileProcessor extends RecursiveTask {
    private long start;
    private long end;
    private long corpusSizeLimit;
    private File[] files;
    private final List<String> keywords;

    public FileProcessor(long start, long end, long corpusSizeLimit, File[] files, List<String> keywords) {
        this.start = start;
        this.end = end;
        this.corpusSizeLimit = corpusSizeLimit;
        this.files = files;
        this.keywords = keywords;
    }

    private Map<String, Integer> countOccurrences(Object input, List<String> keywords) throws Exception {
        Map<String, Integer> occurrences = new HashMap<>();
        for (String key : keywords) {
            occurrences.put(key, 0);
        }

        Scanner scanner;
        if (input instanceof File) {
            scanner = new Scanner((File) input);
        } else if (input instanceof byte[]) {
            scanner = new Scanner(new String((byte[]) input));
        } else {
            throw new IllegalArgumentException("Invalid input type: " + input.getClass());
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (String keyword : keywords) {
                int currentValue = occurrences.get(keyword);
                occurrences.put(keyword, currentValue + countKeywordOccurrences(line, keyword));
            }
        }
        scanner.close();
        return occurrences;
    }
    private int countKeywordOccurrences(String line, String keyword) {
        int count = 0;
        String[] words = line.split("\\W");
        for (String word : words) {
            if (word.equals(keyword)) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected Object compute() {
        Map<String, Integer> result = new HashMap<>();
        for (String key: keywords) {
            result.put(key, 0);
        }

        if (end - start <= corpusSizeLimit) {
            Map<String, Integer> map;
            for (File file : files) {
                try {
                    if (files.length == 1) {
                        RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "r");
                        byte[] bytes = new byte[(int) (end - start)];
                        raf.seek(start); // Move the file pointer to the Xth byte
                        raf.readFully(bytes);
                        map = countOccurrences(bytes, keywords);
                    } else {
                        map = countOccurrences(file, keywords);
                    }
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
            FileProcessor left;
            FileProcessor right;

            // divide by number of files
            if (files.length > 1) {
                int mid = files.length / 2;
                long endLeft = sizeOfFiles(0, mid);
                long endRight = sizeOfFiles(mid, files.length);
                left = new FileProcessor(0, endLeft, corpusSizeLimit, Arrays.copyOfRange(files, 0, mid), keywords);
                right = new FileProcessor(0, endRight, corpusSizeLimit, Arrays.copyOfRange(files, mid, files.length), keywords);
            }
            else { // divide by size
                long mid = ((end - start) / 2) + start;
                left = new FileProcessor(start, mid, corpusSizeLimit, files, keywords);
                right = new FileProcessor(mid, end, corpusSizeLimit, files, keywords);
            }

            left.fork();

            Map<String, Integer> rightResult = (Map<String, Integer> ) right.compute();

            Map<String, Integer> leftResult = (Map<String, Integer> ) left.join();

            for (String key : rightResult.keySet()) {
                int value1 = rightResult.get(key);
                int value2 = leftResult.get(key);
                result.put(key, value1 + value2);
            }
        }
        return result;
    }

    private long sizeOfFiles(int start, int end) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += files[i].length();
        }
        return sum;
    }

}

// end je zbirna velicina fajlova ako se salje copyOfRange i start je 0
// ako imamo samo jedan fajl onda to delimo binarnom