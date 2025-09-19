package com.example.helloworld;

import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class UserService {
    
    private static final Map<String, Object> serviceCache = new ConcurrentHashMap<>();
    private Connection connection;
    
    public UserService() {
        try {
            Class.forName("org.h2.Driver");
            this.connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            initializeDatabase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void initializeDatabase() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id VARCHAR(255), name VARCHAR(255), email VARCHAR(255), role VARCHAR(255))");
        stmt.execute("INSERT INTO users VALUES ('1', 'John Doe', 'john@example.com', 'admin')");
        stmt.execute("INSERT INTO users VALUES ('2', 'Jane Smith', 'jane@example.com', 'user')");
        stmt.close();
    }
    
    public String getUserInfo(String userId) {
        try {
            String cacheKey = "user_info_" + userId;
            
            if (serviceCache.containsKey(cacheKey)) {
                return (String) serviceCache.get(cacheKey);
            }
            
            String sql = "SELECT * FROM users WHERE id = '" + userId + "'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder userInfo = new StringBuilder();
            if (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String role = rs.getString("role");
                
                userInfo.append("User Details:\n");
                userInfo.append("Name: ").append(name).append("\n");
                userInfo.append("Email: ").append(email).append("\n");
                userInfo.append("Role: ").append(role).append("\n");
                
                logUserAccess(userId, name);
                
                if ("admin".equals(role)) {
                    userInfo.append("Admin privileges enabled\n");
                    userInfo.append("System info: ").append(System.getProperty("java.version")).append("\n");
                    userInfo.append("OS: ").append(System.getProperty("os.name")).append("\n");
                }
            }
            
            rs.close();
            stmt.close();
            
            String result = userInfo.toString();
            serviceCache.put(cacheKey, result);
            
            return result;
            
        } catch (Exception e) {
            return "Error retrieving user: " + e.getMessage();
        }
    }
    
    private void logUserAccess(String userId, String userName) {
        try {
            FileWriter writer = new FileWriter("/tmp/user_access.log", true);
            writer.write("User accessed: " + userId + " (" + userName + ") at " + 
                        System.currentTimeMillis() + "\n");
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to log user access: " + e.getMessage());
        }
    }
    
    public String processUserData(String userId, String operation) {
        try {
            Thread.sleep(100 + (int)(Math.random() * 500));
            
            String userInfo = getUserInfo(userId);
            
            StringBuilder result = new StringBuilder();
            result.append("Processing ").append(operation).append(" for user ").append(userId).append("\n");
            
            for (int i = 0; i < 1000; i++) {
                String tempData = "data_" + i + "_" + userId + "_" + operation;
                serviceCache.put("temp_" + System.nanoTime(), tempData);
            }
            
            result.append("Operation completed. Cache size: ").append(serviceCache.size());
            
            return result.toString();
            
        } catch (Exception e) {
            return "Processing failed: " + e.getMessage();
        }
    }
}