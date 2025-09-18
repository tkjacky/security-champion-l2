package secchamp;

import java.io.Serializable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP-specific book information class for REST Template demonstrations
 * This class is designed to work with Spring's HttpEntity for POST operations
 */
public class secchamp_http implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String title;
    private String author;
    private String category;
    private String httpMethod;
    private String endpoint;
    private String apiBaseUrl;
    private ObjectMapper objectMapper;
    
    // Default constructor (required for reflection instantiation)
    public secchamp_http() {
        this.title = "Default Book";
        this.author = "System";
        this.category = "General";
        this.httpMethod = "POST";
        this.endpoint = "/api/books";
        this.apiBaseUrl = "http://springboot-app:8080";
        this.objectMapper = new ObjectMapper();
    }
    
    // Constructor with parameters
    public secchamp_http(String title, String author, String category) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.httpMethod = "POST";
        this.endpoint = "/api/books";
        this.apiBaseUrl = "http://springboot-app:8080";
        this.objectMapper = new ObjectMapper();
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    // Method that gets called during object creation (for demonstration)
    public String getInfo() {
        return String.format("HTTP Book: %s by %s [%s] -> %s %s", 
                           title, author, category, httpMethod, endpoint);
    }
    
    // Override toString for better debugging
    @Override
    public String toString() {
        return getInfo();
    }
    
    // Static method that could be called via reflection
    public static String getClassInfo() {
        return "secchamp_http: HTTP-enabled book information class for REST operations";
    }
    
    // Method to simulate HTTP request preparation
    public String prepareHttpRequest() {
        return String.format("Preparing %s request to %s for book: %s", 
                           httpMethod, endpoint, title);
    }
    
    // Method to fetch real book data from Spring Boot API
    public String fetchBookData() {
        try {
            String fullUrl = apiBaseUrl + "/api/books";
            URL url = new URL(fullUrl);
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
                
                // Parse JSON response and return formatted data
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                JsonNode dataArray = jsonNode.get("data");
                int count = jsonNode.get("count").asInt();
                
                StringBuilder result = new StringBuilder();
                result.append("HTTP Request Success - Found ").append(count).append(" books: ");
                
                if (dataArray != null && dataArray.isArray()) {
                    for (int i = 0; i < Math.min(3, dataArray.size()); i++) {
                        JsonNode book = dataArray.get(i);
                        result.append(book.get("title").asText()).append(" by ").append(book.get("author").asText());
                        if (i < Math.min(2, dataArray.size() - 1)) result.append(", ");
                    }
                    if (dataArray.size() > 3) result.append("...");
                }
                
                return result.toString();
            } else {
                return "HTTP Request Failed - Response code: " + responseCode;
            }
        } catch (Exception e) {
            return "HTTP Request Error: " + e.getMessage();
        }
    }
    
    // Method to fetch book reviews data
    public String fetchBookReviews() {
        try {
            String fullUrl = apiBaseUrl + "/api/books/reviews";
            URL url = new URL(fullUrl);
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
                JsonNode dataArray = jsonNode.get("data");
                int count = jsonNode.get("count").asInt();
                
                return "HTTP Book Reviews - Found " + count + " reviews in database";
            } else {
                return "HTTP Request Failed - Response code: " + responseCode;
            }
        } catch (Exception e) {
            return "HTTP Request Error: " + e.getMessage();
        }
    }
    
    // Method to search books via HTTP
    public String searchBooksHttp(String searchTerm) {
        try {
            String searchUrl = apiBaseUrl + "/api/books/search?title=" + java.net.URLEncoder.encode(searchTerm, "UTF-8");
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
                    result.append("HTTP Search Result - Found ").append(count).append(" books for '").append(searchTerm).append("': ");
                    for (JsonNode book : booksArray) {
                        result.append(book.get("title").asText()).append(" ($").append(book.get("price").asText()).append("); ");
                    }
                    return result.toString();
                } else {
                    return "HTTP Search - No books found for '" + searchTerm + "'";
                }
            } else {
                return "HTTP Search Failed - Response code: " + responseCode;
            }
        } catch (Exception e) {
            return "HTTP Search Error: " + e.getMessage();
        }
    }
}
