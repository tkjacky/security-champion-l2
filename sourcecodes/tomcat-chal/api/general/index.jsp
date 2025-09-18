<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.net.*, java.util.Date, java.lang.reflect.*" %>
<%
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
response.setHeader("Access-Control-Allow-Headers", "Content-Type");

String className = request.getParameter("className");
if (className == null) {
    className = "unknown";
}

try {
    StringBuilder resultInfo = new StringBuilder();
    String dangerLevel = "SAFE";
    String description = "";
    String reflectionDetails = "";
    
    // Use actual Java reflection to load the class
    Class<?> clazz = Class.forName(className);
    reflectionDetails = "Class successfully loaded via reflection: " + clazz.getName();
    
    if ("java.lang.Runtime".equals(className)) {
        dangerLevel = "EXTREMELY_DANGEROUS";
        description = "Runtime class access - can execute system commands";
        try {
            // Use reflection to get Runtime instance and invoke methods
            Method getRuntimeMethod = clazz.getMethod("getRuntime");
            Object runtimeInstance = getRuntimeMethod.invoke(null);
            
            Method getAvailableProcessorsMethod = clazz.getMethod("availableProcessors");
            int processors = (Integer) getAvailableProcessorsMethod.invoke(runtimeInstance);
            
            Method getFreeMemoryMethod = clazz.getMethod("freeMemory");
            long freeMemory = (Long) getFreeMemoryMethod.invoke(runtimeInstance);
            
            String osName = System.getProperty("os.name");
            
            resultInfo.append("SECURITY RISK: Runtime accessed via reflection! ");
            resultInfo.append("OS: ").append(osName).append(", ");
            resultInfo.append("Processors: ").append(processors).append(", ");
            resultInfo.append("Free Memory: ").append(freeMemory).append(" bytes");
            
            reflectionDetails += " - Methods invoked: getRuntime(), availableProcessors(), freeMemory()";
        } catch (Exception e) {
            resultInfo.append("Runtime reflection execution failed: ").append(e.getMessage());
        }
        
    } else if ("java.lang.System".equals(className)) {
        dangerLevel = "DANGEROUS";
        description = "System class access - can read system properties";
        try {
            // Use reflection to access System.getProperty method
            Method getPropertyMethod = clazz.getMethod("getProperty", String.class);
            
            String javaVersion = (String) getPropertyMethod.invoke(null, "java.version");
            String userHome = (String) getPropertyMethod.invoke(null, "user.home");
            String osArch = (String) getPropertyMethod.invoke(null, "os.arch");
            
            resultInfo.append("SECURITY RISK: System accessed via reflection! ");
            resultInfo.append("Java: ").append(javaVersion).append(", ");
            resultInfo.append("User Home: ").append(userHome).append(", ");
            resultInfo.append("OS Arch: ").append(osArch);
            
            reflectionDetails += " - Method invoked: getProperty() with multiple system properties";
        } catch (Exception e) {
            resultInfo.append("System reflection execution failed: ").append(e.getMessage());
        }
        
    } else if ("java.io.File".equals(className)) {
        dangerLevel = "DANGEROUS";
        description = "File class access - can access file system";
        try {
            // Use reflection to create File instance and invoke methods
            Constructor<?> fileConstructor = clazz.getConstructor(String.class);
            Object fileInstance = fileConstructor.newInstance("/");
            
            Method existsMethod = clazz.getMethod("exists");
            boolean exists = (Boolean) existsMethod.invoke(fileInstance);
            
            Method listRootsMethod = clazz.getMethod("listRoots");
            Object[] roots = (Object[]) listRootsMethod.invoke(null);
            
            resultInfo.append("SECURITY RISK: File system accessed via reflection! ");
            resultInfo.append("Root path exists: ").append(exists).append(", ");
            resultInfo.append("Available roots: ").append(roots.length);
            
            reflectionDetails += " - Methods invoked: constructor, exists(), listRoots()";
        } catch (Exception e) {
            resultInfo.append("File reflection execution failed: ").append(e.getMessage());
        }
        
    } else if ("java.net.Socket".equals(className)) {
        dangerLevel = "DANGEROUS";
        description = "Socket class access - can create network connections";
        try {
            // Use reflection to check Socket class methods
            Method[] methods = clazz.getMethods();
            int networkMethods = 0;
            for (Method m : methods) {
                if (m.getName().contains("connect") || m.getName().contains("bind")) {
                    networkMethods++;
                }
            }
            
            resultInfo.append("SECURITY RISK: Socket class accessed via reflection! ");
            resultInfo.append("Available network methods: ").append(networkMethods).append(", ");
            resultInfo.append("Can create network connections.");
            
            reflectionDetails += " - Analyzed " + methods.length + " methods, found " + networkMethods + " network-related methods";
        } catch (Exception e) {
            resultInfo.append("Socket reflection analysis failed: ").append(e.getMessage());
        }
        
    } else if ("java.io.FileWriter".equals(className)) {
        dangerLevel = "EXTREMELY_DANGEROUS";
        description = "FileWriter class access - can write arbitrary files";
        try {
            // Use reflection to create FileWriter and write file
            String filename = "/tmp/reflection_helloworld_" + System.currentTimeMillis() + ".txt";
            String content = "Hello from Java reflection attack! Timestamp: " + new Date();
            
            Constructor<?> writerConstructor = clazz.getConstructor(String.class);
            Object writerInstance = writerConstructor.newInstance(filename);
            
            Method writeMethod = clazz.getMethod("write", String.class);
            writeMethod.invoke(writerInstance, content);
            
            Method closeMethod = clazz.getMethod("close");
            closeMethod.invoke(writerInstance);
            
            resultInfo.append("SECURITY RISK: File written via reflection! ");
            resultInfo.append("Created: ").append(filename).append(" ");
            resultInfo.append("with content: ").append(content.substring(0, Math.min(50, content.length()))).append("...");
            
            reflectionDetails += " - Methods invoked: constructor, write(), close()";
        } catch (Exception e) {
            resultInfo.append("FileWriter reflection execution failed: ").append(e.getMessage()).append(" - but class was loaded");
        }
        
    } else if ("evil.Info".equals(className)) {
        dangerLevel = "EXTREMELY_DANGEROUS";
        description = "Evil Info class - exposes system information and performs attacks";
        try {
            // Load the actual compiled evil.Info class directly
            evil.Info evilInstance = new evil.Info();
            
            String systemInfo = evilInstance.getSystemInfo();
            String privData = evilInstance.getPrivilegedData();
            String fileResult = evilInstance.writeFile();
            String networkResult = evilInstance.networkProbe();
            
            resultInfo.append("EXTREMELY DANGEROUS: Evil.Info loaded directly! ");
            resultInfo.append("SystemInfo: ").append(systemInfo).append(" | ");
            resultInfo.append("PrivData: ").append(privData).append(" | ");
            resultInfo.append("FileWrite: ").append(fileResult).append(" | ");
            resultInfo.append("NetworkProbe: ").append(networkResult);
            
            reflectionDetails += " - Direct class loading: evil.Info instantiated and methods called";
        } catch (Exception e) {
            resultInfo.append("Evil.Info direct loading failed: ").append(e.getMessage());
        }
        
    } else if ("secchamp.Info".equals(className)) {
        dangerLevel = "SAFE";
        description = "SecChamp Info class - legitimate book categorization";
        try {
            // Load the actual compiled secchamp.Info class directly
            secchamp.Info secchampInstance = new secchamp.Info();
            
            String appInfo = secchampInstance.getAppInfo();
            String timestamp = secchampInstance.getTimestamp();
            String categoryInfo = secchampInstance.getCategoryInfo();
            
            resultInfo.append("SAFE: SecChamp.Info loaded directly. ");
            resultInfo.append("AppInfo: ").append(appInfo).append(" | ");
            resultInfo.append("Timestamp: ").append(timestamp).append(" | ");
            resultInfo.append("CategoryInfo: ").append(categoryInfo);
            
            reflectionDetails += " - Direct class loading: secchamp.Info instantiated and methods called";
        } catch (Exception e) {
            resultInfo.append("SecChamp.Info direct loading failed: ").append(e.getMessage());
        }
        
    } else {
        dangerLevel = "UNKNOWN";
        description = "Unknown class type - basic reflection analysis";
        try {
            // Basic reflection analysis for unknown classes
            Constructor<?>[] constructors = clazz.getConstructors();
            Method[] methods = clazz.getMethods();
            Field[] fields = clazz.getFields();
            
            resultInfo.append("Class reflection analysis: ");
            resultInfo.append("Constructors: ").append(constructors.length).append(", ");
            resultInfo.append("Methods: ").append(methods.length).append(", ");
            resultInfo.append("Fields: ").append(fields.length);
            
            reflectionDetails += " - Basic analysis: " + constructors.length + " constructors, " + methods.length + " methods, " + fields.length + " fields";
        } catch (Exception e) {
            resultInfo.append("Basic reflection analysis failed: ").append(e.getMessage());
        }
    }
    
    String jsonResponse = "{"
        + "\"status\": \"success\","
        + "\"className\": \"" + className.replace("\"", "\\\"") + "\","
        + "\"dangerLevel\": \"" + dangerLevel + "\","
        + "\"description\": \"" + description.replace("\"", "\\\"") + "\","
        + "\"result\": \"" + resultInfo.toString().replace("\"", "\\\"").replace("\n", "\\n") + "\","
        + "\"reflectionDetails\": \"" + reflectionDetails.replace("\"", "\\\"") + "\","
        + "\"timestamp\": \"" + new Date() + "\","
        + "\"service\": \"General Java Class Reflection API\""
        + "}";
    
    out.print(jsonResponse);
} catch (ClassNotFoundException e) {
    String errorResponse = "{"
        + "\"status\": \"error\","
        + "\"className\": \"" + className.replace("\"", "\\\"") + "\","
        + "\"message\": \"Class not found via reflection: " + e.getMessage().replace("\"", "\\\"") + "\","
        + "\"dangerLevel\": \"ERROR\","
        + "\"reflectionDetails\": \"ClassNotFoundException - class could not be loaded\""
        + "}";
    out.print(errorResponse);
} catch (Exception e) {
    String errorResponse = "{"
        + "\"status\": \"error\","
        + "\"className\": \"" + className.replace("\"", "\\\"") + "\","
        + "\"message\": \"Reflection error: " + e.getMessage().replace("\"", "\\\"") + "\","
        + "\"dangerLevel\": \"ERROR\","
        + "\"reflectionDetails\": \"Exception during reflection: " + e.getClass().getSimpleName() + "\""
        + "}";
    out.print(errorResponse);
}
%>
