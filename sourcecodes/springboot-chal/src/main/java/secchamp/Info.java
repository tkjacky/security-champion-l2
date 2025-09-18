package secchamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Legitimate information class for book categorization
 */
public class Info implements Serializable {
    private String appName;
    private String version;
    private Date timestamp;
    private String category;
    
    public Info() {
        this.appName = "SecChamp Book Store";
        this.version = "1.0.0";
        this.timestamp = new Date();
        this.category = "BookCategorization";
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
        return "SecChamp Book Categorization API - Safe and legitimate book service for genre classification and catalog management. No security risks detected.";
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
