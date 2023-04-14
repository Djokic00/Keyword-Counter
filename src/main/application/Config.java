package main.application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config {

    private static Config instance = null;
    public String file_corpus_prefix;
    public long dir_crawler_sleep_time;
    public List<String> keywords;
    public long file_scanning_size_limit;
    public int hop_count;
    public long url_refresh_time;


    public Config() {
        Properties props = new Properties();
        try {
            InputStream input = new FileInputStream("src/resources/app.properties");
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        keywords =  Arrays.asList(props.getProperty("keywords").split(","));
        file_corpus_prefix = props.getProperty("file_corpus_prefix");
        dir_crawler_sleep_time = Integer.parseInt(props.getProperty("dir_crawler_sleep_time"));
        file_scanning_size_limit = Integer.parseInt(props.getProperty("file_scanning_size_limit"));
        hop_count = Integer.parseInt(props.getProperty("hop_count"));
        url_refresh_time = Long.parseLong(props.getProperty("url_refresh_time"));
    }

    public static Config getInstance() {
        if (instance == null)
            instance = new Config();
        return instance;
    }
}
