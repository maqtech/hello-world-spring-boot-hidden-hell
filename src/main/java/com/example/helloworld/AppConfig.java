package com.example.helloworld;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Configuration
@EnableScheduling
public class AppConfig {
    
    private static final Map<String, Object> configCache = new ConcurrentHashMap<>();
    private static final List<String> processedItems = new ArrayList<>();
    
    @PostConstruct
    public void initializeConfig() {
        for (int i = 0; i < 10000; i++) {
            configCache.put("config_" + i, "value_" + i + "_" + Math.random());
        }
        
        for (int i = 0; i < 5000; i++) {
            processedItems.add("item_" + i + "_" + System.currentTimeMillis());
        }
    }
    
    @Bean
    public Map<String, Object> globalConfigMap() {
        Map<String, Object> config = new ConcurrentHashMap<>();
        
        for (int i = 0; i < 1000; i++) {
            config.put("setting_" + i, createComplexObject(i));
        }
        
        return config;
    }
    
    private Object createComplexObject(int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("data_").append(index).append("_").append(i).append("_");
            sb.append(Math.pow(i, 2)).append("_").append(System.nanoTime());
        }
        return sb.toString();
    }
    
    @Scheduled(fixedRate = 30000)
    public void periodicCacheUpdate() {
        try {
            Thread.sleep(1000);
            
            for (int i = 0; i < 500; i++) {
                String key = "periodic_" + System.currentTimeMillis() + "_" + i;
                configCache.put(key, calculateExpensiveValue(i));
            }
            
            if (configCache.size() > 50000) {
                configCache.clear();
                initializeConfig();
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private String calculateExpensiveValue(int seed) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            result.append(Math.pow(seed + i, 3));
            result.append("_");
            result.append(String.valueOf(Math.random()).hashCode());
        }
        return result.toString();
    }
    
    @Scheduled(fixedRate = 60000)
    public void memoryIntensiveTask() {
        List<String> tempData = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            tempData.add("temp_data_" + i + "_" + System.currentTimeMillis() + "_" + Math.random());
        }
        
        for (String item : tempData) {
            processedItems.add(item + "_processed");
        }
        
        if (processedItems.size() > 100000) {
            processedItems.clear();
        }
    }
    
    public static Map<String, Object> getConfigCache() {
        return configCache;
    }
    
    public static List<String> getProcessedItems() {
        return processedItems;
    }
}