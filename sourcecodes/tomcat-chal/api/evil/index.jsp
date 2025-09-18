<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="evil.Info" %>
<%
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
response.setHeader("Access-Control-Allow-Headers", "Content-Type");

try {
    // Check if a specific class is being requested
    String requestedClass = request.getParameter("className");
    
    if (requestedClass != null) {
        if ("secchamp.Info".equals(requestedClass)) {
            // Evil API should not have access to secchamp.Info class
            String errorResponse = "{"
                + "\"status\": \"error\","
                + "\"message\": \"Class not found: secchamp.Info is not available in Evil API\","
                + "\"service\": \"Evil Information Exposure API\","
                + "\"type\": \"DANGEROUS\","
                + "\"error\": \"ClassNotFoundException: secchamp.Info not found in evil namespace\","
                + "\"warning\": \"Attempted to access legitimate class from evil endpoint!\""
                + "}";
            out.print(errorResponse);
            return;
        } else if (!"evil.Info".equals(requestedClass)) {
            // Only evil.Info is available in this API
            String errorResponse = "{"
                + "\"status\": \"error\","
                + "\"message\": \"Class not found: \" + requestedClass + \" is not available in Evil API\","
                + "\"service\": \"Evil Information Exposure API\","
                + "\"type\": \"DANGEROUS\","
                + "\"error\": \"ClassNotFoundException: Only evil.Info is available\","
                + "\"warning\": \"Evil API accessed but wrong class requested!\""
                + "}";
            out.print(errorResponse);
            return;
        }
    }
    
    // Load the actual compiled evil.Info class and execute its dangerous methods
    Info evilInfo = new Info();
    
    String systemInfo = evilInfo.getSystemInfo();
    String privilegedData = evilInfo.getPrivilegedData();
    String fileWriteResult = evilInfo.writeFile();
    String networkProbe = evilInfo.networkProbe();
    String message = evilInfo.getMessage();
    
    String jsonResponse = "{"
        + "\"status\": \"success\","
        + "\"service\": \"Evil Information Exposure API\","
        + "\"systemInfo\": \"" + systemInfo.replace("\"", "\\\"").replace("\n", "\\n") + "\","
        + "\"privilegedData\": \"" + privilegedData.replace("\"", "\\\"").replace("\n", "\\n") + "\","
        + "\"fileWriteResult\": \"" + fileWriteResult.replace("\"", "\\\"").replace("\n", "\\n") + "\","
        + "\"networkProbe\": \"" + networkProbe.replace("\"", "\\\"").replace("\n", "\\n") + "\","
        + "\"message\": \"" + message.replace("\"", "\\\"").replace("\n", "\\n") + "\","
        + "\"type\": \"EXTREMELY_DANGEROUS\""
        + "}";
    
    out.print(jsonResponse);
} catch (Exception e) {
    String errorResponse = "{"
        + "\"status\": \"error\","
        + "\"message\": \"Evil service error: " + e.getMessage().replace("\"", "\\\"") + "\","
        + "\"type\": \"DANGEROUS\","
        + "\"warning\": \"Even errors expose that evil class was loaded!\""
        + "}";
    out.print(errorResponse);
}
%>