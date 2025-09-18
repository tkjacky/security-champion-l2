package evil;

import java.io.*;
import java.net.*;
import java.util.Date;

/**
 * Evil information class that exposes system information and performs dangerous operations
 */
public class Info implements Serializable {
    private String systemInfo;
    private String osInfo;
    private String fileSystemInfo;
    
    public Info() {
        this.systemInfo = "EvilInfo initialized";
        this.osInfo = collectOSInfo();
        this.fileSystemInfo = "File operations available";
    }
    
    public String getSystemInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String javaVersion = System.getProperty("java.version");
            String userHome = System.getProperty("user.home");
            String userName = System.getProperty("user.name");
            
            return "SYSTEM ACCESS GRANTED! OS: " + osName + " " + osVersion + 
                   ", Java: " + javaVersion + ", User: " + userName + 
                   ", Home: " + userHome + ", Processors: " + runtime.availableProcessors() +
                   ", Free Memory: " + runtime.freeMemory() + " bytes";
        } catch (Exception e) {
            return "System info collection failed: " + e.getMessage();
        }
    }
    
    public String getPrivilegedData() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("PRIVILEGED DATA ACCESS! ");
            sb.append("System Properties: ");
            sb.append("file.separator=" + System.getProperty("file.separator") + ", ");
            sb.append("path.separator=" + System.getProperty("path.separator") + ", ");
            sb.append("line.separator=" + System.getProperty("line.separator").length() + " chars, ");
            sb.append("java.class.path=" + System.getProperty("java.class.path").substring(0, Math.min(100, System.getProperty("java.class.path").length())) + "...");
            return sb.toString();
        } catch (Exception e) {
            return "Privileged data access failed: " + e.getMessage();
        }
    }
    
    public String writeFile() {
        try {
            String filename = "/tmp/evil_helloworld_" + System.currentTimeMillis() + ".txt";
            String content = "EVIL FILE WRITTEN! Timestamp: " + new Date() + 
                           "\nSystem: " + System.getProperty("os.name") + 
                           "\nUser: " + System.getProperty("user.name") +
                           "\nJava: " + System.getProperty("java.version") +
                           "\nThis file was created by evil reflection attack!";
            
            FileWriter writer = new FileWriter(filename);
            writer.write(content);
            writer.close();
            
            return "EXTREMELY DANGEROUS: Evil file written successfully to " + filename + 
                   " with " + content.length() + " bytes. Content preview: " + 
                   content.substring(0, Math.min(80, content.length())) + "...";
        } catch (Exception e) {
            return "Evil file write failed: " + e.getMessage() + " - but evil class was loaded!";
        }
    }
    
    public String networkProbe() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("NETWORK PROBE ACTIVE! ");
            
            // Check localhost connectivity
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("localhost", 8080), 1000);
                socket.close();
                sb.append("localhost:8080 REACHABLE, ");
            } catch (Exception e) {
                sb.append("localhost:8080 unreachable, ");
            }
            
            // Get network interface info
            try {
                NetworkInterface.getNetworkInterfaces();
                sb.append("Network interfaces accessible, ");
            } catch (Exception e) {
                sb.append("Network interfaces blocked, ");
            }
            
            sb.append("Evil network reconnaissance completed!");
            return sb.toString();
        } catch (Exception e) {
            return "Network probe failed: " + e.getMessage();
        }
    }
    
    private String collectOSInfo() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version");
    }
    
    public String getMessage() {
        return "EXTREMELY DANGEROUS: Evil Information Exposure API - This service performs unauthorized system reconnaissance, exposes sensitive data, writes malicious files, and conducts network probing. SECURITY BREACH DETECTED!";
    }
    
    // Getters
    public String getOsInfo() { return osInfo; }
    public String getFileSystemInfo() { return fileSystemInfo; }
}
