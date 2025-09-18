package secchamp;

import java.io.Serializable;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Legitimate information class for book categorization
 */
public class Info implements Serializable {
    private String appName;
    private String version;
    private Date timestamp;
    private String category;
    private String bookApiUrl;
    private ObjectMapper objectMapper;
    
    public Info() {
        this.appName = "SecChamp Book Store";
        this.version = "1.0.0";
        this.timestamp = new Date();
        this.category = "BookCategorization";
        this.bookApiUrl = "http://springboot-app:8080/api/books";
        this.objectMapper = new ObjectMapper();
    }
    
    public String getAppInfo() {
        return "Application: " + appName + ", Version: " + version + ", Category: " + category;
    }
    
    public String getTimestamp() {
        return "Current time: " + timestamp.toString();
    }
    
    public String getCategoryInfo() {
        return "Safe book categorization service - handles genre classification";
    }
    
    public String getMessage() {
        return "SecChamp Book Categorization API - Safe and legitimate book service for genre classification and catalog management. No security risks detected. Connected to live book database.";
    }
    
    // Method to fetch real book data from Spring Boot API
    public String getBookData() {
        try {
            URL url = new URL(bookApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse JSON response
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                JsonNode dataArray = jsonNode.get("data");
                
                if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode firstBook = dataArray.get(0);
                    return String.format("Live Book Data - Title: %s, Author: %s, Category: %s, Rating: %s, Price: $%s", 
                        firstBook.get("title").asText(),
                        firstBook.get("author").asText(),
                        firstBook.get("category").asText(),
                        firstBook.get("rating").asText(),
                        firstBook.get("price").asText()
                    );
                } else {
                    return "No book data available";
                }
            } else {
                return "Failed to fetch book data. Response code: " + responseCode;
            }
        } catch (Exception e) {
            return "Error fetching book data: " + e.getMessage();
        }
    }
    
    // Method to get book count from API
    public String getBookCount() {
        try {
            URL url = new URL(bookApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                int count = jsonNode.get("count").asInt();
                return "Total books in database: " + count;
            } else {
                return "Failed to get book count. Response code: " + responseCode;
            }
        } catch (Exception e) {
            return "Error getting book count: " + e.getMessage();
        }
    }
    
    // Method to search books by title
    public String searchBooks(String title) {
        try {
            String searchUrl = bookApiUrl + "/search?title=" + java.net.URLEncoder.encode(title, "UTF-8");
            URL url = new URL(searchUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                JsonNode booksArray = jsonNode.get("books");
                int count = jsonNode.get("count").asInt();
                
                if (count > 0) {
                    StringBuilder result = new StringBuilder();
                    result.append("Found ").append(count).append(" books matching '").append(title).append("': ");
                    for (JsonNode book : booksArray) {
                        result.append(book.get("title").asText()).append(" by ").append(book.get("author").asText()).append("; ");
                    }
                    return result.toString();
                } else {
                    return "No books found matching '" + title + "'";
                }
            } else {
                return "Failed to search books. Response code: " + responseCode;
            }
        } catch (Exception e) {
            return "Error searching books: " + e.getMessage();
        }
    }
    
    // Getters and setters
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public Date getTimestampObj() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
