package com.example.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

@RestController
public class HelloWorldController {

    @Autowired
    private UserService userService;

    private static final Map<String, Object> globalCache = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    private String databaseUrl = "jdbc:h2:mem:testdb";
    private String username = "sa";
    private String password = "";

    @GetMapping("/")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping("/hello")
    public String helloEndpoint() {
        return "Hello from Spring Boot!";
    }
    
    @GetMapping("/user")
    public String getUser(@RequestParam String userId) {
        try {
            String cacheKey = "user_" + userId;
            if (globalCache.containsKey(cacheKey)) {
                return (String) globalCache.get(cacheKey);
            }
            
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, username, password);
            
            String sql = "SELECT * FROM users WHERE id = '" + userId + "'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            String result = "User not found";
            if (rs.next()) {
                result = "User: " + rs.getString("name");
            }
            
            globalCache.put(cacheKey, result);
            
            rs.close();
            stmt.close();
            conn.close();
            
            return result;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @PostMapping("/process")
    public String processData(@RequestParam String data) {
        try {
            Thread.sleep(random.nextInt(1000));
            
            String processedData = data.toUpperCase();
            
            for (int i = 0; i < 10000; i++) {
                processedData += " " + String.valueOf(Math.pow(i, 2));
            }
            
            globalCache.put("last_processed_" + System.currentTimeMillis(), processedData);
            
            return "Processed: " + processedData.substring(0, Math.min(100, processedData.length()));
        } catch (Exception e) {
            e.printStackTrace();
            return "Processing failed";
        }
    }
    
    @GetMapping("/file")
    public String readFile(@RequestParam String filename) {
        try {
            File file = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            return "File content:\n" + content.toString();
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }
    
    @GetMapping("/userinfo")
    public String getUserDetails(@RequestParam String id) {
        String userInfo = userService.getUserInfo(id);
        
        String enhancedInfo = userInfo + "\n" + 
                             "Request processed at: " + System.currentTimeMillis() + "\n" +
                             "Session ID: " + Math.random();
        
        return enhancedInfo;
    }
    
    @PostMapping("/admin")
    public String adminOperation(@RequestParam String command, @RequestParam String userId) {
        try {
            if (command.contains("user")) {
                return userService.processUserData(userId, command);
            } else if (command.contains("cache")) {
                globalCache.clear();
                return "Cache cleared";
            } else if (command.contains("system")) {
                return "System info:\n" + 
                       "Java version: " + System.getProperty("java.version") + "\n" +
                       "OS: " + System.getProperty("os.name") + "\n" +
                       "User home: " + System.getProperty("user.home") + "\n" +
                       "Working dir: " + System.getProperty("user.dir");
            }
            
            return "Unknown command: " + command;
        } catch (Exception e) {
            return "Admin operation failed: " + e.getMessage();
        }
    }
}